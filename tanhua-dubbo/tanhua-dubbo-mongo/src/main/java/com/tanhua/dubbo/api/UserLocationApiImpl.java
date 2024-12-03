package com.tanhua.dubbo.api;

import cn.hutool.core.collection.CollUtil;
import com.mongodb.client.MongoClient;
import com.tanhua.model.mongo.UserLocation;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@DubboService
public class UserLocationApiImpl implements UserLocationApi{
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MongoClient mongo;

    /**
     * 上报地理信息
     */
    @Override
    public Boolean uploadLocation(Long userId, Double latitude, Double longitude, String addrStr) {
        try {
            //检查mongodb中有没有旧数据
            Query query = new Query(Criteria.where("userId").is(userId));
            UserLocation location = mongoTemplate.findOne(query, UserLocation.class);
            //没有就直接插入
            if (location == null) {
                //封装数据
                location = new UserLocation();
                location.setUserId(userId);
                location.setLocation(new GeoJsonPoint(longitude, latitude));
                location.setAddress(addrStr);
                location.setCreated(System.currentTimeMillis());
                location.setUpdated(System.currentTimeMillis());
                location.setLastUpdated(System.currentTimeMillis());
                //更新
                mongoTemplate.save(location);
            } else {
                //有就更新
                Update update = Update.update("location", new GeoJsonPoint(longitude, latitude))
                        .set("address", addrStr)
                        .set("updated", System.currentTimeMillis())
                        .set("lastUpdated", System.currentTimeMillis());
                mongoTemplate.updateFirst(query, update, UserLocation.class);
            }
            //返回插入成功
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            //更新失败
            return false;
        }
    }


    /**
     * 搜附近的人
     */
    @Override
    public List<Long> getNearUsers(Long userId, Double distance) {
        //获取当前用户的位置信息
        Query query = new Query(Criteria.where("userId").is(userId));
        UserLocation location = mongoTemplate.findOne(query, UserLocation.class);
        if (location == null) {
            return new ArrayList<Long>();
        }
        //找圆心
        GeoJsonPoint point = location.getLocation();
        //设置半径
        Distance dis = new Distance(distance / 1000, Metrics.KILOMETERS);
        //画圆
        Circle circle = new Circle(point, dis);
        //查询
        Query locationQuery = new Query(Criteria.where("location").withinSphere(circle));
        List<UserLocation> userLocations = mongoTemplate.find(locationQuery, UserLocation.class);
        List<Long> ids = CollUtil.getFieldValues(userLocations, "userId", Long.class);
        return ids;
    }
}
