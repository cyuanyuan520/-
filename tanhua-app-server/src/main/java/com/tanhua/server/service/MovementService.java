package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.CommentApi;
import com.tanhua.dubbo.api.MovementApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VisitorsApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.enums.CommentType;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.mongo.Visitors;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.MovementsVo;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.VisitorsVo;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MovementService {

    @Autowired
    private OssTemplate ossTemplate;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @DubboReference
    private MovementApi movementApi;
    @DubboReference
    private UserInfoApi userInfoApi;
    @DubboReference
    private CommentApi commentApi;
    @DubboReference
    private VisitorsApi visitorsApi;

    /**
     * 发布圈子动态
     * @param movement
     * @param imageContent
     * @throws IOException
     */
    public void publishMovement(Movement movement, MultipartFile[] imageContent) throws IOException {
        //判断当前发布的内容是否存在
        if (movement == null) {
            throw new BusinessException(ErrorResult.contentError());
        }
        //获取当前操作用户id
        Long userId = UserHolder.getUserId();
        //上传用户发布的图片到阿里云oss
        ArrayList<String> list = new ArrayList<>();
        for (MultipartFile file : imageContent) {
            String upload = ossTemplate.upload(file.getOriginalFilename(), new BufferedInputStream(file.getInputStream()));
            list.add(upload);
        }
        //构造movement对象
        movement.setUserId(userId);
        movement.setMedias(list);
        //调用dubbo远程使用movementApi
        movementApi.publish(movement);
    }

    /**
     * 获取某个userId的动态
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult getMovementById(Long userId, int page, int pagesize) {
        //获取符合当前条件(id, page, pagesize)的所有动态(pageResult)
        PageResult pr = movementApi.getMovementById(userId, page, pagesize);
        if (pr.getItems().isEmpty()) {
            return pr;
        }
        List<Movement> movements = (List<Movement>) pr.getItems();
        List<MovementsVo> vos = getVoResult(movements);
        pr.setItems(vos);
        return pr;
    }

    /**
     * 查询好友的动态
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult getFriendMovements(int page, int pagesize) {
        //获取当前操作人id
        Long userId = UserHolder.getUserId();
        //dubbo: 查询出所有movement详情
        List<Movement> list = movementApi.getFriendMovement(userId, page, pagesize);//movement实际上对应的是集合里的friendId
        if (list == null || list.size() == 0) {
            //检查查询出来的movement集合是否为空 是的话返回一个空pageResult
            return new PageResult();
        }
        List<MovementsVo> voResult = getVoResult(list);
        PageResult pr = new PageResult();
        pr.setItems(voResult);
        pr.setPage(page);
        pr.setPagesize(pagesize);
        return pr;
    }


    /**
     * 推荐动态
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult getRecommandMovements(int page, int pagesize) {
        //获取当前用户id
        Long userId = UserHolder.getUserId();
        String redisKey = Constants.MOVEMENTS_RECOMMEND + UserHolder.getUserId();
        //获取redis中推荐动态的pid(是一个字符串)
        String value = redisTemplate.opsForValue().get(redisKey);
        List<Long> pids = new ArrayList<>();
        List<Movement> movements = new ArrayList<>();
        if (StringUtils.isEmpty(value)) {
            //如果redis中没查询到数据: 调用mongodbApi随机产生pagesize条movement记录
            movements = movementApi.getRandomMovements(pagesize);
        } else {
            //分割pid(变成集合)
            String[] split = value.split(",");
            //如果当前页开始的index都大于数组length了: 直接抛异常
            if ((page - 1) * pagesize >= split.length) {
                throw new BusinessException(ErrorResult.contentError());
            }
            pids = Arrays.stream(split).skip((page - 1) * pagesize).limit(pagesize).map(item -> {
                return Long.valueOf(item);
            }).collect(Collectors.toList());
            //dubbo: 调用api 把pids集合变成movement集合
            movements = movementApi.getRecommandMovement(pids);
        }
        //封装成movementVo
        List<MovementsVo> voResult = getVoResult(movements);
        PageResult pr = new PageResult();
        pr.setItems(voResult);
        pr.setPage(page);
        pr.setPagesize(pagesize);
        return pr;
    }


    /**
     * 传入Movement List 自动给每个movement查询用户id 然后封装成voList
     * @param list
     * @return
     */
    private List<MovementsVo> getVoResult(List<Movement> list) {
        Long nowId = UserHolder.getUserId();//当前用户id
        //将所有movement的userId提取出来
        List<Long> idList = CollUtil.getFieldValues(list, "userId", Long.class);
        Map<Long, UserInfo> infoByIds = userInfoApi.getInfoByIds(idList, null);//key是userId,value是对应的UserInfo
        ArrayList<MovementsVo> vos = new ArrayList<>();//构造最后要放在pageResult中的vos集合
        for (Movement movement : list) {
            Long id = movement.getUserId();//动态作者id
            UserInfo userInfo = infoByIds.get(id);
            MovementsVo vo = MovementsVo.init(userInfo, movement);
            //检测redis中是否已经有当前账号点赞数据
            String key = Constants.MOVEMENTS_INTERACT_KEY + movement.getId().toString();//圈子互动key
            String hashKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getUserId();//圈子互动key内部的hashKey(like)
            String loveHashKey = Constants.MOVEMENT_LOVE_HASHKEY + UserHolder.getUserId();//圈子互动key内部的hashKey(love)
            Boolean hasKey = redisTemplate.opsForHash().hasKey(key, hashKey);
            Boolean hasLoveKey = redisTemplate.opsForHash().hasKey(key, loveHashKey);
            if (hasKey) {
                vo.setHasLiked(1);
            }
            if (hasLoveKey) {
                vo.setHasLoved(1);
            }
            vos.add(vo);
        }
        return vos;
    }

    /**
     * 查询某一条具体动态
     * @param id
     * @return
     */
    public MovementsVo getMovementByMId(String id) {
        Movement movement = movementApi.getMovementByMId(id);
        if (movement == null) {
            return null;
        }
        Long userId = movement.getUserId();
        UserInfo userInfo = userInfoApi.findById(userId);
        MovementsVo vo = MovementsVo.init(userInfo, movement);
        return vo;
    }


    /**
     * 查看首页访客列表
     */
    public List<VisitorsVo> visitors() {
        //获取redis中的上次访问时间
        String key = Constants.VISITORS_USER;
        String hashKey = UserHolder.getUserId().toString();
        String tempDate = (String) redisTemplate.opsForHash().get(key, hashKey);//获得上次查看访客列表的毫秒数
        Long date = tempDate == null ? null : Long.valueOf(tempDate);
        //获取新访客记录
        List<Visitors> visitors = visitorsApi.datedVisitors(date, UserHolder.getUserId(), 1, 5);
        if (CollUtil.isEmpty(visitors)){
            //集合为空: 直接返回空集合
            return new ArrayList<>();
        }
        List<Long> visitorUserId = CollUtil.getFieldValues(visitors, "visitorUserId", Long.class);//提取来访用户的id集合
        Map<Long, UserInfo> infoByIds = userInfoApi.getInfoByIds(visitorUserId, null);
        //封装List<VisitorsVo>
        List<VisitorsVo> vos = new ArrayList<>();
        for (Visitors visitor : visitors) {
            UserInfo userInfo = infoByIds.get(visitor.getVisitorUserId());
            if (userInfo != null) {
                VisitorsVo vo = VisitorsVo.init(userInfo, visitor);
                vos.add(vo);
            }
        }
        //可能应该更新redis中上次访问时间
        return vos;
    }
}
