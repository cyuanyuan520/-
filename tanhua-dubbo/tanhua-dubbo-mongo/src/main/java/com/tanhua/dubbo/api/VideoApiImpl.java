package com.tanhua.dubbo.api;


import cn.hutool.core.collection.CollUtil;
import com.tanhua.dubbo.utils.IdWorker;
import com.tanhua.model.mongo.FocusUser;
import com.tanhua.model.mongo.Video;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@DubboService
public class VideoApiImpl implements VideoApi {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IdWorker idWorker;

    /**
     * 上传视频
     * 返回视频的id表示上传成功
     */
    @Override
    public String save(Video video) {
        video.setVid(idWorker.getNextId("video"));
        video.setCreated(System.currentTimeMillis());
        mongoTemplate.save(video);
        return video.getId().toHexString();
    }


    /**
     * 随机获取needSize个vid
     * 如果传入了vidList 需要追加条件: 返回的vid必须是在vidList没出现过的
     */
    @Override
    public List<Long> randomVid(List<Long> vidList, int needSize) {
        Criteria criteria = new Criteria();
        //判断是否为空
        if (CollUtil.isNotEmpty(vidList)) {
            criteria.and("vid").nin(vidList);
        }
        //构造统计数据
        TypedAggregation<Video> aggregation = new TypedAggregation<>(Video.class, Aggregation.match(criteria), Aggregation.sample(needSize));
        AggregationResults<Video> aggregate = mongoTemplate.aggregate(aggregation, Video.class);
        List<Video> videos = aggregate.getMappedResults();
        List<Long> vids = CollUtil.getFieldValues(videos, "vid", Long.class);
        return vids;
    }

    /**
     * 根据vidList寻找到视频
     */
    @Override
    public ArrayList<Video> findVideosByVid(List<Long> vidList) {
        Query query = new Query(Criteria.where("vid").in(vidList));
        List<Video> videos = mongoTemplate.find(query, Video.class);
        return (ArrayList<Video>) videos;
    }

    /**
     * 关注视频作者
     * 关注前先检查之前是不是已经关注 防止db中出现两条一模一样的数据
     */
    @Override
    public void follow(FocusUser focusUser) {
        //保存前检查是否已经存在对应数据
        Query query = new Query(Criteria.where("userId").is(focusUser.getUserId())
                .and("followUserId").is(focusUser.getFollowUserId()));
        //是就直接return
        if (mongoTemplate.exists(query, FocusUser.class)) {
            return;
        }
        mongoTemplate.save(focusUser);
    }


    /**
     * 取关视频作者
     * @param focusUser
     */
    @Override
    public void unFollow(FocusUser focusUser) {
        Query query = new Query(Criteria.where("userId").is(focusUser.getUserId())
                .and("followUserId").is(focusUser.getFollowUserId()));
        mongoTemplate.remove(query, FocusUser.class);
    }

}
