package com.tanhua.dubbo.api;


import cn.hutool.core.collection.CollUtil;
import com.tanhua.model.mongo.RecommendUser;
import com.tanhua.model.mongo.UserLike;
import com.tanhua.model.vo.PageResult;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@DubboService
public class RecommendUserApiImpl implements RecommendUserApi {

    @Autowired
    MongoTemplate mongoTemplate;

    /**
     * 查询今日佳人
     * @param userId
     * @return
     */
    public RecommendUser getRecommendUser(Long userId) {
        Query query = new Query(Criteria.where("toUserId").is(userId))
                .with(Sort.by(Sort.Order.desc("score")))
                .limit(1);
        RecommendUser recommendUser = mongoTemplate.findOne(query, RecommendUser.class);
        return recommendUser;
    }

    @Override
    public PageResult getRecommendList(Integer page, Integer pagesize, Long userId) {
        //构造查询条件
        Query query = new Query(Criteria.where("toUserId").is(userId))
                .skip((page - 1) * pagesize)
                .limit(pagesize);
        //查询总数
        long count = mongoTemplate.count(query, RecommendUser.class);
        //查询RecommendUser
        List<RecommendUser> recommandList = mongoTemplate.find(query, RecommendUser.class);
        //封装返回数据
        return new PageResult(page, pagesize, count, recommandList);
    }


    /**
     * 查询佳人信息
     */
    @Override
    public RecommendUser getUserById(long id, Long userId) {
        //构造查询对象
        Query query = new Query(Criteria.where("userId").is(id)
                .and("toUserId").is(userId));
        RecommendUser recommendUser = mongoTemplate.findOne(query, RecommendUser.class);
        if (recommendUser == null) {
            Random random = new Random();
            recommendUser = new RecommendUser();
            recommendUser.setUserId(id);
            recommendUser.setToUserId(userId);
            recommendUser.setScore(random.nextDouble() * 100);
        }
        return recommendUser;
    }

    /**
     * 随机查询出推荐的用户列表
     * 统计函数
     * 传入的是我自己的id
     */
    @Override
    public List<RecommendUser> getRecommendCards(Long userId) {
        //根据自己的id: 查询出已经点过喜欢或不喜欢的ids
        Query likeQuery = new Query(Criteria.where("userId").is(userId));
        List<UserLike> likedUsers = mongoTemplate.find(likeQuery, UserLike.class);
        List<Long> likedIds = CollUtil.getFieldValues(likedUsers, "likeUserId", Long.class);
        //构造推荐用户查询条件: 不出现已经点过喜欢/不喜欢的人
        Criteria criteria = Criteria.where("toUserId").is(userId).and("userId").nin(likedUsers);
        //构造统计函数
        TypedAggregation<RecommendUser> aggregation = new TypedAggregation<>(RecommendUser.class,
                Aggregation.match(criteria), Aggregation.sample(10));
        //查询
        AggregationResults<RecommendUser> aggregate = mongoTemplate.aggregate(aggregation, RecommendUser.class);
        //返回
        return aggregate.getMappedResults();
    }

}
