package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.CommentApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.enums.CommentType;
import com.tanhua.model.mongo.Comment;
import com.tanhua.model.vo.CommentVo;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.PageResult;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);
    @DubboReference
    private CommentApi commentApi;
    @DubboReference
    private UserInfoApi userInfoApi;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 发布评论
     * @param movementId
     * @param content
     */
    public void saveComment(String movementId, String content) {
        //获取当前操作用户id
        Long userId = UserHolder.getUserId();
        //封装comment对象
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(movementId));
        comment.setCommentType(CommentType.COMMENT.getType());
        comment.setContent(content);
        comment.setUserId(userId);
        //还差被评论人的id
        comment.setCreated(System.currentTimeMillis());
        Integer num = commentApi.save(comment);
        log.info(String.valueOf(num));
    }

    /**
     * 查询评论列表
     */
    public PageResult getComments(String movementId, int page, int pagesize) {
        //调用dubbo: 查询出评论列表
        PageResult pageResult = commentApi.getComments(movementId, CommentType.COMMENT, page, pagesize);
        List<Comment> comments = (List<Comment>) pageResult.getItems();
        if (CollUtil.isEmpty(comments)) {
            return pageResult;
        }
        //获取发布人id
        List<Long> userIds = CollUtil.getFieldValues(comments, "userId", Long.class);
        //dubbo: 获取发布人信息
        Map<Long, UserInfo> infoByIds = userInfoApi.getInfoByIds(userIds,null);
        //循环:封装成CommentsVo
        ArrayList<CommentVo> commentVos = new ArrayList<>();//用来存储CommentVo
        for (Comment comment : comments) {
            //当前评论作者id:
            Long userId = comment.getUserId();
            UserInfo userInfo = infoByIds.get(userId);
            CommentVo vo = CommentVo.init(userInfo, comment);
            //检查redis中是否点过赞
            String key = Constants.COMMENT_INTERACT_KEY + comment.getId();
            String hashKey = Constants.COMMENT_LIKE_HASHKEY + UserHolder.getUserId();
            Boolean exist = redisTemplate.opsForHash().hasKey(key, hashKey);
            if (exist) {
                //存在则标记已点赞
                vo.setHasLiked(1);
            }
            commentVos.add(vo);
        }
        //封装最终数据
        pageResult.setItems(commentVos);
        return pageResult;
    }

    /**
     * 点赞圈子动态
     * @param movementId
     * @return
     */
    public Integer like(String movementId) {
        //检查是否已经给这个帖子点赞(为什么要封装成一个方法呢? 因为可以复用!)
        boolean hasLiked = commentApi.hasComment(movementId, UserHolder.getUserId(), CommentType.LIKE);
        if (hasLiked) {
            throw new BusinessException(ErrorResult.likeError());
        }
        //将数据保存到mongodb
            //封装comment数据
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(movementId));
        comment.setCommentType(CommentType.LIKE.getType());
        comment.setUserId(UserHolder.getUserId());
            //还差被评论人id
        comment.setCreated(System.currentTimeMillis());
            //dubbo调用api
        Integer count = commentApi.save(comment);//这步操作会自动填充被评论人id
        //redis(HashKey)中保存已点赞
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;//圈子互动key
        String hashKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getUserId();//圈子互动key内部的hashKey
        redisTemplate.opsForHash().delete(key, hashKey);
        //返回点赞后like总数
        return count;
    }

    /**
     * 取消圈子点赞
     * @param movementId
     * @return
     */
    public Integer disLike(String movementId) {
        //检查是否已经点过赞(没有就抛出异常)
        boolean hasComment = commentApi.hasComment(movementId, UserHolder.getUserId(), CommentType.LIKE);
        if (!hasComment) {
            throw new BusinessException(ErrorResult.disLikeError());
        }
        //删除mongodb中的comment数据并且对应movement的like数量-1
                //封装comment
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(movementId));
        comment.setUserId(UserHolder.getUserId());
        comment.setCommentType(CommentType.LIKE.getType());
                //调用api删除like/love数据 对应的count减一
        Integer count = commentApi.deleteComment(comment);
        //删除redis中的数据
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;//圈子互动key
        String hashKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getUserId();//圈子互动key内部的hashKey
        redisTemplate.opsForHash().delete(key, hashKey);
        //返回取消点赞后的like总数
        return count;
    }


    /**
     * love圈子动态
     * @param movementId
     * @return
     */
    public Integer love(String movementId) {
        //检查是否已经给这个帖子点love(为什么要封装成一个方法呢? 因为可以复用!)
        boolean hasLoved = commentApi.hasComment(movementId, UserHolder.getUserId(), CommentType.LOVE);
        if (hasLoved) {
            throw new BusinessException(ErrorResult.loveError());
        }
        //将数据保存到mongodb
        //封装comment数据
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(movementId));
        comment.setCommentType(CommentType.LOVE.getType());
        comment.setUserId(UserHolder.getUserId());
        //还差被评论人id
        comment.setCreated(System.currentTimeMillis());
        //dubbo调用api
        Integer count = commentApi.save(comment);//这步操作会自动填充被评论人id
        //redis(HashKey)中保存已点赞
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;//圈子互动key
        String hashKey = Constants.MOVEMENT_LOVE_HASHKEY + UserHolder.getUserId();//圈子互动key内部的hashKey
        redisTemplate.opsForHash().put(key, hashKey, "1");
        //返回点赞后love总数
        return count;
    }


    /**
     * 取消love圈子动态
     * @param movementId
     * @return
     */
    public Integer unLove(String movementId) {
        //检查是否已经点过love(没有就抛出异常)
        boolean hasComment = commentApi.hasComment(movementId, UserHolder.getUserId(), CommentType.LOVE);
        if (!hasComment) {
            throw new BusinessException(ErrorResult.disloveError());
        }
        //删除mongodb中的comment数据并且对应movement的love数量-1
        //封装comment
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(movementId));
        comment.setUserId(UserHolder.getUserId());
        comment.setCommentType(CommentType.LOVE.getType());
        //调用api删除like/love数据 对应的count减一
        Integer count = commentApi.deleteComment(comment);
        //删除redis中的数据
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;//圈子互动key
        String hashKey = Constants.MOVEMENT_LOVE_HASHKEY + UserHolder.getUserId();//圈子互动key内部的hashKey
        redisTemplate.opsForHash().delete(key, hashKey);
        //返回取消love后的love总数
        return count;
    }


    /**
     * 给动态底下的评论点赞
     * @param commentId
     * @return
     */
    public Integer likeComment(String commentId) {
        //检查redis中是否已经存在点赞数据(有就抛出异常)
        String key = Constants.COMMENT_INTERACT_KEY + commentId;
        String hashKey = Constants.COMMENT_LIKE_HASHKEY + UserHolder.getUserId();
        Boolean exist = redisTemplate.opsForHash().hasKey(key, hashKey);
        if (exist) {
            //如果已经点赞就抛出异常
            throw new BusinessException(ErrorResult.likeError());
        }
        //操作redis存储点赞数据
        redisTemplate.opsForHash().put(key, hashKey, "1");
        //mongodb中对应评论的点赞数加1
        Integer count = commentApi.likeComment(commentId);
        //返回更新完成后的点赞数
        return count;
    }

    /**
     * 评论取消点赞
     * @param commentId
     * @return
     */
    public Integer dislikeComment(String commentId) {
        //检查redis中是否已经存在点赞数据(有就抛出异常)
        String key = Constants.COMMENT_INTERACT_KEY + commentId;
        String hashKey = Constants.COMMENT_LIKE_HASHKEY + UserHolder.getUserId();
        Boolean exist = redisTemplate.opsForHash().hasKey(key, hashKey);
        if (!exist) {
            //如果没有点赞就抛出异常
            throw new BusinessException(ErrorResult.disLikeError());
        }
        //操作redis删除点赞数据
        redisTemplate.opsForHash().delete(key, hashKey);
        //mongodb中对应评论的点赞数-1
        Integer count = commentApi.dislikeComment(commentId);
        //返回更新完成后的点赞数
        return count;
    }
}
