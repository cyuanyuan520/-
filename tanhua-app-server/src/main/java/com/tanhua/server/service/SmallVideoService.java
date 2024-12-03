package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VideoApi;
import com.tanhua.dubbo.api.VideoCommentApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.mongo.Comment;
import com.tanhua.model.mongo.FocusUser;
import com.tanhua.model.mongo.Video;
import com.tanhua.model.vo.CommentVo;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.VideoVo;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SmallVideoService {

    @Autowired
    private FastFileStorageClient storageClient;
    @Autowired
    private FdfsWebServer webServer;
    @Autowired
    private OssTemplate ossTemplate;
    @Autowired
    private RestTemplate restTemplate;
    @DubboReference
    private VideoApi videoApi;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @DubboReference
    private UserInfoApi userInfoApi;
    @Autowired
    private ReactiveStringRedisTemplate reactiveStringRedisTemplate;
    @DubboReference
    private VideoCommentApi videoCommentApi;


    /**
     * 上传视频(和封面)
     */
    public void uploadVideo(MultipartFile videoThumbnail, MultipartFile videoFile) throws IOException {
        if (videoThumbnail.isEmpty() || videoFile.isEmpty()) {
            //文件为空直接抛异常
            log.error("上传的文件为空!");
            throw new BusinessException(ErrorResult.error());
        }
        //上传视频
        String filename = videoFile.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        StorePath path = storageClient.uploadFile(new BufferedInputStream(videoFile.getInputStream()), videoFile.getSize(), suffix, null);
        //上传视频封面
        String imgPath = ossTemplate.upload(videoThumbnail.getOriginalFilename(), new BufferedInputStream(videoThumbnail.getInputStream()));
        //dubbo: api保存
            //封装数据
        Video video = new Video();
        video.setUserId(UserHolder.getUserId());
        String yiYan = restTemplate.getForObject("https://uapis.cn/api/say", String.class);
        video.setText(yiYan);
        video.setPicUrl(imgPath);
        video.setVideoUrl(webServer.getWebServerUrl() + path.getFullPath());

        String videoId = videoApi.save(video);
        if (StringUtils.isEmpty(videoId)) {
            //没有传回视频id直接抛出异常
            throw new BusinessException(ErrorResult.error());
        }
    }

    /**
     * 查看视频列表
     */
    @Cacheable(value = "videos", key = "T(com.tanhua.server.interceptor.UserHolder).getUserId() + '_' + #page + '_' + #pagesize")
    public PageResult getVideoList(Integer page, Integer pagesize) {
        //获取当前操作用户id
        Long userId = UserHolder.getUserId();
        //在redis中获取推荐视频列表
        String key = Constants.VIDEOS_RECOMMEND + userId;
        String vids = redisTemplate.opsForValue().get(key);//String类型的vid: 100001,100002,100003,100004,100005
        ArrayList<Video> videos = new ArrayList<>();//最后用来存视频数据的地方
        if (vids != null && !vids.isEmpty()) {
            String[] split = vids.split(",");
            if ((page- 1) * pagesize < split.length) {
                List<Long> vidList = Arrays.stream(split).skip((page - 1) * pagesize).limit(pagesize).map(v -> {
                    return Long.valueOf(v);
                }).collect(Collectors.toList());//得到了vid集合
                //可能会出现redis中推荐视频个数不够一页的情况(随机采样vid 并且不能出现已经出现过的视频 补全pagesize)
                int needSize = pagesize - vidList.size();
                List<Long> vidList2 = videoApi.randomVid(vidList, needSize);
                vidList.addAll(vidList2);
                videos = videoApi.findVideosByVid(vidList);
            }
        }
        //redis里压根没有数据时:
        if (StringUtils.isEmpty(vids)) {
            List<Long> vidList3 = videoApi.randomVid(null, pagesize);
            videos = videoApi.findVideosByVid(vidList3);
        }
        //根据用户id查询出用户详情
        List<Long> userIds = CollUtil.getFieldValues(videos, "userId", Long.class);
        Map<Long, UserInfo> infoByIds = userInfoApi.getInfoByIds(userIds, null);
        //封装
        List<VideoVo> vos = new ArrayList<>();
        for (Video video : videos) {
            UserInfo userInfo = infoByIds.get(video.getUserId());
            if (userInfo != null) {
                VideoVo vo = VideoVo.init(userInfo, video);
                //检查是否关注
                if (redisTemplate.opsForHash().hasKey(Constants.FOCUS_USER + UserHolder.getUserId(), String.valueOf(userInfo.getId()))){
                    //是就封个1
                    vo.setHasFocus(1);
                }
                //检查是否点赞
                if (redisTemplate.opsForHash().hasKey(Constants.VIDEO_LIKE_HASHKEY, UserHolder.getUserId() + "_"+ video.getId())) {
                    //是就封个1
                    vo.setHasLiked(1);
                }
                vos.add(vo);
            }
        }
        //返回
        return new PageResult(page, pagesize, 0L, vos);
    }

    /**
     * 关注视频作者
     * 传过来的是作者id
     */
    public void follow(long uid) {
        //获取操作用户id
        Long userId = UserHolder.getUserId();
        //redis: 保存关注数据
        String key = Constants.FOCUS_USER + userId;
        String hashKey = String.valueOf(uid);
        if (redisTemplate.opsForHash().hasKey(key,hashKey)) {
            //已经点过关注: 直接抛出异常
            throw new BusinessException(ErrorResult.followError());
        }
        redisTemplate.opsForHash().put(key, hashKey, "1");
        //mongoDb: 调api保存到数据库
            //封装(没封装保存时间)
        FocusUser focusUser = new FocusUser();
        focusUser.setUserId(userId);
        focusUser.setFollowUserId(uid);
            //保存
        videoApi.follow(focusUser);
    }

    /**
     * 取关视频作者
     * 传过来的是作者id
     */
    public void unFollow(long uid) {
        //获取操作用户id
        Long userId = UserHolder.getUserId();
        //redis: 删除关注数据
        String key = Constants.FOCUS_USER + userId;
        String hashKey = String.valueOf(uid);
        if (!redisTemplate.opsForHash().hasKey(key, hashKey)) {
            //没关注就取关: 直接抛出异常
            throw new BusinessException(ErrorResult.unFollowError());
        }
        redisTemplate.opsForHash().delete(key, hashKey, "1");
        //mongoDb: 调api操作数据库
        //封装(没封装保存时间)
        FocusUser focusUser = new FocusUser();
        focusUser.setUserId(userId);
        focusUser.setFollowUserId(uid);
        //保存
        videoApi.unFollow(focusUser);
    }

    /**
     * 点赞视频
     */
    public void likeVideo(String id) {
        //检查redis中点赞数据是否存在
            //存在:抛出异常
        String key = Constants.VIDEO_LIKE_HASHKEY;
        String hashKey = UserHolder.getUserId() + "_" + id;
        if (redisTemplate.opsForHash().hasKey(key, hashKey)) {
            throw new BusinessException(ErrorResult.likeError());
        }
        //redis中保存点赞数据
        redisTemplate.opsForHash().put(key, hashKey, "1");
        //mongodb中保存数据 并且点赞数+1
            //封装数据(没封装被评论人ID)
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(id));
        comment.setCommentType(1);
        comment.setUserId(UserHolder.getUserId());
        comment.setCreated(System.currentTimeMillis());
            //保存
        videoCommentApi.save(comment);
    }

    /**
     * 取消点赞视频
     */
    public void dislikeVideo(String id) {
        //检查redis中点赞数据是否存在
            //不存在:抛出异常
        String key = Constants.VIDEO_LIKE_HASHKEY;
        String hashKey = UserHolder.getUserId() + "_" + id;
        if (!redisTemplate.opsForHash().hasKey(key, hashKey)) {
            throw new BusinessException(ErrorResult.disLikeError());
        }
        //redis中移除点赞数据
        redisTemplate.opsForHash().delete(key, hashKey);
        //mongodb中移除点赞数据
            //封装数据
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(id));
        comment.setCommentType(1);
        comment.setUserId(UserHolder.getUserId());
        comment.setCreated(System.currentTimeMillis());
        videoCommentApi.disLike(comment);
    }


    /**
     * 添加评论
     * @param id
     * @param content
     */
    public void addComment(String id, String content) {
        //封装数据
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(id));
        comment.setCommentType(2);
        comment.setUserId(UserHolder.getUserId());
        comment.setCreated(System.currentTimeMillis());
        comment.setContent(content);
        //调用api
        videoCommentApi.save(comment);
    }


    /**
     * 评论列表
     */
    public PageResult getComment(String id, Integer page, Integer pagesize) {
        //获取视频评论列表
        List<Comment> comments = videoCommentApi.getCommentList(id, page, pagesize);
        List<Long> publishUserId = CollUtil.getFieldValues(comments, "userId", Long.class);
        //根据用户id找到useInfo
        Map<Long, UserInfo> infoByIds = userInfoApi.getInfoByIds(publishUserId, null);
        //封装数据
        List<CommentVo> vos = new ArrayList<>();
        for (Comment comment : comments) {
            UserInfo userInfo = infoByIds.get(comment.getPublishUserId());
            if (userInfo != null) {
                CommentVo vo = CommentVo.init(userInfo, comment);
                //判断本条评论是否已经点赞过
                String key = Constants.COMMENT_LIKE_HASHKEY + comment.getId();//key
                String hashKey = String.valueOf(UserHolder.getUserId());
                if (redisTemplate.opsForHash().hasKey(key, hashKey)) {
                    vo.setHasLiked(1);
                }
                vos.add(vo);
            }
        }
        return new PageResult(page, pagesize, 0L, vos);
    }

    /**
     * 点赞视频下方评论
     * 传入评论id
     */
    public void likeComment(String id) {
        //已点赞数据记录到redis里
        String key = Constants.COMMENT_LIKE_HASHKEY + id;//key
        String hashKey = String.valueOf(UserHolder.getUserId());
        redisTemplate.opsForHash().put(key, hashKey, "1");
        //mongodb: likeCount+1
        videoCommentApi.likeComment(id);
    }
    /**
     * 取消点赞视频下方评论
     * 传入评论id
     */
    public void dislikeComment(String id) {
        //已点赞数据从redis里移除
        String key = Constants.COMMENT_LIKE_HASHKEY + id;//key
        String hashKey = String.valueOf(UserHolder.getUserId());
        redisTemplate.opsForHash().delete(key, hashKey, "1");
        //mongodb: likeCount-1
        videoCommentApi.dislikeComment(id);
    }
}
