package com.tanhua.server.service;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.*;
import com.tanhua.model.domain.Question;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.dto.RecommendUserDto;
import com.tanhua.model.mongo.RecommendUser;
import com.tanhua.model.mongo.Visitors;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.NearUserVo;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.TodayBest;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class TanhuaService {

    @DubboReference
    private RecommendUserApi recommendUserApi;
    @DubboReference
    private UserInfoApi userInfoApi;
    @DubboReference
    private QuestionApi questionApi;
    @DubboReference
    private UserLikeApi userLikeApi;
    @DubboReference
    private UserLocationApi userLocationApi;
    @DubboReference
    private VisitorsApi visitorsApi;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private HuanXinTemplate huanXinTemplate;
    @Autowired
    private MessageService messageService;

    @Value("${tanhua.default.recommend.users}")
    private String recommendUser;

    public PageResult getRecommendList(RecommendUserDto recommendUserDto) {
        //获取dto信息
        Integer page = recommendUserDto.getPage();//当前页码
        Integer pagesize = recommendUserDto.getPagesize();//每页数据条数
        Long userId = UserHolder.getUserId();//用户id
        //dubbo: 获取mongodb中的今日推荐名单
        PageResult pr = recommendUserApi.getRecommendList(page, pagesize, userId);//mongodb
        List<RecommendUser> recommandUsers = (List<RecommendUser>) pr.getItems();
        if (recommandUsers == null || recommandUsers.size() == 0) {
            return pr;
        }
        //构建出需要批量查询的userId列表(防止需要多次使用userInfoApi 造成提供者性能和网络拥堵)
        List<Long> ids = CollUtil.getFieldValues(recommandUsers, "userId", Long.class);//CollUtil
        //调用dubbo中的UsrInfoApi把详细信息查出来(还有限定条件)
        UserInfo info = new UserInfo();
        info.setGender(recommendUserDto.getGender());
        info.setAge(recommendUserDto.getAge());
        Map<Long, UserInfo> map = userInfoApi.getInfoByIds(ids, info);
        ArrayList<TodayBest> list = new ArrayList<>();//用来放最后要返回的pageResult的item
        //封装todayBest
        for (RecommendUser rUser : recommandUsers) {
            UserInfo userInfo = map.get(rUser.getUserId());
            if (userInfo != null) {
                TodayBest todayBest = TodayBest.init(userInfo, rUser);
                list.add(todayBest);
            }
        }
        pr.setItems(list);
        return pr;
    }

    /**
     * 查询佳人信息
     * 保存访客记录
     */
    public TodayBest getInfoById(long id) {
        //根据id查询userINfo
        UserInfo userInfo = userInfoApi.findById(id);
        //根据userId和当前操作人Id查询出recommendUser信息
        RecommendUser recommendUser = recommendUserApi.getUserById(id, UserHolder.getUserId());

        //构造访客信息
        Visitors visitor = new Visitors();
        visitor.setUserId(id);
        visitor.setVisitorUserId(UserHolder.getUserId());
        visitor.setFrom("首页");
        visitor.setDate(System.currentTimeMillis());
        visitor.setVisitDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        visitor.setScore(recommendUser.getScore());
        //调用api保存数据
        visitorsApi.save(visitor);

        //构造todayBest对象并返回
        TodayBest todayBest = TodayBest.init(userInfo, recommendUser);
        return todayBest;
    }

    /**
     *查看陌生人问题
     */
    public String getStrangerQuestions(String userId) {
        Question question = questionApi.findByUserId(Long.valueOf(userId));
        String txt = question == null ? "你喜欢java企业级开发吗?" : question.getTxt();
        return txt;
    }

    /**
     * 回复陌生人问题
     */
    public void replyQuestions(Long userId, String reply) {
        //构造要发送的json数据
        Long myId = UserHolder.getUserId();
        UserInfo userInfo = userInfoApi.findById(myId);
        HashMap map = new HashMap();
        map.put("userId", myId);
        map.put("huanXinId", Constants.HX_USER_PREFIX + myId);
        map.put("nickname", userInfo.getNickname());
        map.put("strangerQuestion", getStrangerQuestions(String.valueOf(userId)));
        map.put("reply", reply);
        String json = JSON.toJSONString(map);
        //使用环信template发送数据
        Boolean res = huanXinTemplate.sendMsg(Constants.HX_USER_PREFIX + userId, json);
        if (!res) {
            throw new BusinessException(ErrorResult.error());
        }
    }

    /**
     * 探花: 左滑右滑
     */
    public List<TodayBest> getCards() {
        //查询出recommendUserList
        List<RecommendUser> recommendUsers = recommendUserApi.getRecommendCards(UserHolder.getUserId());
            //如果推荐用户为空: 加载默认数据
        if (CollUtil.isEmpty(recommendUsers)) {
            String[] strings = recommendUser.split(",");
            for (String string : strings) {
                RecommendUser user = new RecommendUser();
                user.setUserId(Long.valueOf(string));//设置被推荐的用户id
                user.setToUserId(UserHolder.getUserId());//设置用户id
                user.setScore(RandomUtil.randomDouble(60, 90));
                recommendUsers.add(user);
            }
        }
        //提取userId 根据userId查询出UserInfo
        List<Long> userIds = CollUtil.getFieldValues(recommendUsers, "userId", Long.class);
        Map<Long, UserInfo> infoByIds = userInfoApi.getInfoByIds(userIds, null);
        //封装todayBest的list
        List<TodayBest> list = new ArrayList<>();
        for (RecommendUser rUser : recommendUsers) {
            Long userId = rUser.getUserId();//别人的userid
            UserInfo userInfo = infoByIds.get(userId);
            TodayBest todayBest = TodayBest.init(userInfo, rUser);
            list.add(todayBest);
        }
        //返回数据
        return list;
    }


    /**
     * 探花: 右滑喜欢
     */
    public void likeUser(long likeUserId) {
        //mongodb中保存或更新数据
        Boolean result = userLikeApi.saveOrUpdate(UserHolder.getUserId(), likeUserId, true);
        if (!result) {
            //发生异常直接抛出 防止数据不一致
            throw new BusinessException(ErrorResult.error());
        }
        //redis中添加数据
            //先删除不喜欢表中的数据(直接删 redis不会报错)
        redisTemplate.opsForSet().remove(Constants.USER_NOT_LIKE_KEY + UserHolder.getUserId(), String.valueOf(likeUserId));
            //再添加喜欢数据
        redisTemplate.opsForSet().add(Constants.USER_LIKE_KEY + UserHolder.getUserId(), String.valueOf(likeUserId));
        //判断是否双向喜欢: 是则添加好友
        if (likeOrNot(likeUserId, UserHolder.getUserId())) {
            messageService.addContact(likeUserId);
        }
    }


    /**
     * 探花: 左滑不喜欢
     */
    public void unLikeUser(long likeUserId) {
        //mongodb中保存或更新数据
        Boolean result = userLikeApi.saveOrUpdate(UserHolder.getUserId(), likeUserId, false);
        if (!result) {
            //发生异常直接抛出 防止数据不一致
            throw new BusinessException(ErrorResult.error());
        }
        //redis中添加数据
            //先删除喜欢set里的数据
        redisTemplate.opsForSet().remove(Constants.USER_LIKE_KEY + UserHolder.getUserId(), String.valueOf(likeUserId));
            //再添加不喜欢set中的数据(直接删 redis不会报错)
        redisTemplate.opsForSet().add(Constants.USER_NOT_LIKE_KEY + UserHolder.getUserId(), String.valueOf(likeUserId));
    }


    /**
     * 查询redis中是否存在一种喜欢关系
     * 是: return true
     */
    private Boolean likeOrNot(Long userId, Long likeUserId) {
        Boolean member = redisTemplate.opsForSet().isMember(Constants.USER_LIKE_KEY + userId, String.valueOf(likeUserId));
        return member;
    }

    /**
     * 搜附近的人
     */
    public List<NearUserVo> searchNear(String gender, Double distance) {
        //dubbo: 调用api查询出全部临近用户的集合
        List<Long> userIds = userLocationApi.getNearUsers(UserHolder.getUserId(), distance);
        if (CollUtil.isEmpty(userIds)) {
            return new ArrayList<NearUserVo>();
        }
        //根据用户id以及性别查询出所有合适的userInfo
        UserInfo info = new UserInfo();
        info.setGender(gender);
        Map<Long, UserInfo> infoByIds = userInfoApi.getInfoByIds(userIds, info);
        //封装最后要返回的nearUserVo
        List<NearUserVo> nearUserVos = new ArrayList<>();
        for (Long userId : userIds) {
            if (userId == UserHolder.getUserId()) {
                //避免把自己查询出来
                continue;
            }
            UserInfo userInfo = infoByIds.get(userId);
            //这个id没有因为附加条件被筛掉了 可能会查出来空的
            if (userInfo != null) {
                nearUserVos.add(NearUserVo.init(userInfo));
            }
        }
        return nearUserVos;
    }
}
