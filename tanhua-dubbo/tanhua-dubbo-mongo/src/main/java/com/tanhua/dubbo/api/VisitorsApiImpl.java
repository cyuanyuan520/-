package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Visitors;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collections;
import java.util.List;

@DubboService
public class VisitorsApiImpl implements VisitorsApi{
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 保存访客数据
     * 同一天的只保存一次
     */
    @Override
    public void save(Visitors visitor) {
        //查看是否存在同一天的数据
        Query query = new Query(Criteria.where("userId").is(visitor.getUserId())
                .and("visitorUserId").is(visitor.getVisitorUserId())
                .and("visitDate").is(visitor.getVisitDate()));
        if (!mongoTemplate.exists(query, Visitors.class)) {
            //不存在则保存
            mongoTemplate.save(visitor);
        }
    }

    /**
     * 查看首页访客列表
     */
    @Override
    public List<Visitors> datedVisitors(Long date, Long userId, Integer page, Integer pagesize) {
        //构造查询条件: 最多显示5条
        Criteria criteria = Criteria.where("userId").is(userId);
        if (date != null) {
            criteria.and("date").gt(date);
        }
        //查询
        Query query = new Query(criteria).skip((page - 1) * pagesize).limit(pagesize);
        List<Visitors> visitors = mongoTemplate.find(query, Visitors.class);
        return visitors;
    }
}
