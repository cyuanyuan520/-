package com.tanhua.dubbo.api;


import com.tanhua.model.mongo.RecommendUser;
import com.tanhua.model.vo.PageResult;

import java.util.List;

public interface RecommendUserApi {
    /**
     * 查询今日佳人
     */
    RecommendUser getRecommendUser(Long userId);

    PageResult getRecommendList(Integer page, Integer pagesize, Long userId);

    RecommendUser getUserById(long id, Long userId);

    List<RecommendUser> getRecommendCards(Long userId);
}
