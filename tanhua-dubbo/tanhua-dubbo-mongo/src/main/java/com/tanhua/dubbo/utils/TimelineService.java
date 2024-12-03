package com.tanhua.dubbo.utils;

import com.tanhua.model.mongo.Friend;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.mongo.MovementTimeLine;
import javafx.animation.Timeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TimelineService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Async
    public void saveTimeline(Movement movement) {
        //存储好友时间线表
        //获取当前用户的全部好友
        Query query = new Query(Criteria.where("userId").is(movement.getUserId()));
        List<Friend> friends = mongoTemplate.find(query, Friend.class);
        //循环好友数据
        ArrayList<MovementTimeLine> line = new ArrayList<>();
        for (Friend friend : friends) {
            MovementTimeLine timeLine = new MovementTimeLine();
            timeLine.setMovementId(movement.getId());
            timeLine.setCreated(movement.getCreated());
            timeLine.setUserId(friend.getUserId());
            timeLine.setFriendId(friend.getFriendId());
            line.add(timeLine);
        }
        mongoTemplate.insertAll(line);
    }
}
