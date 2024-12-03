package com.tanhua.server.service;

import com.tanhua.dubbo.api.RecommendUserApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.mongo.RecommendUser;
import com.tanhua.model.vo.TodayBest;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class TodayBestService {

    @DubboReference
    private RecommendUserApi recommendUserApi;
    @DubboReference
    private UserInfoApi userInfoApi;

    /**
     * 获取今日佳人
     */
    public TodayBest getTodayBest() {
        //获取当前操作用户id
        Long userId = UserHolder.getUserId();
        //调用dubbo接口
        RecommendUser recommendUser = recommendUserApi.getRecommendUser(userId);
        Long id = recommendUser.getUserId();//美女的id
        UserInfo userInfo = userInfoApi.findById(id);
        TodayBest todayBest = TodayBest.init(userInfo, recommendUser);//今日推荐的详细信息
        return todayBest;
    }

}
