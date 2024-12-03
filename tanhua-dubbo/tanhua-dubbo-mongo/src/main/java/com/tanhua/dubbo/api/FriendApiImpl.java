package com.tanhua.dubbo.api;

import com.mongodb.client.MongoClient;
import com.tanhua.model.mongo.Friend;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collections;
import java.util.List;

@DubboService
public class FriendApiImpl implements FriendApi{

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 添加好友关系
     */
    @Override
    public void addContact(Long userId, Long friendId) {
        //"我"有没有添加"她"
        Query query = new Query(Criteria.where("userId").is(userId).and("friendId").is(friendId));
        if (!mongoTemplate.exists(query, Friend.class)) {
            //mongodb中不存在"我"和"对方"的好友关系: 可以添加
            Friend friend1 = new Friend();
            friend1.setUserId(userId);
            friend1.setFriendId(friendId);
            friend1.setCreated(System.currentTimeMillis());
            mongoTemplate.save(friend1);
        }
        //"她"有没有添加"我"
        Query query1 = new Query(Criteria.where("userId").is(friendId).and("friendId").is(userId));
        if (!mongoTemplate.exists(query1, Friend.class)) {
            //mongodb中不存在"她"和"我"的好友关系: 可以添加
            Friend friend2 = new Friend();
            friend2.setUserId(friendId);
            friend2.setFriendId(userId);
            friend2.setCreated(System.currentTimeMillis());
            mongoTemplate.save(friend2);
        }
    }

    /**
     *  查询好友列表
     *  传入的是自己的id
     * @return
     */
    @Override
    public List<Friend> getFriendsById(Integer page, Integer pagesize, Long userId) {
        Query query = new Query(Criteria.where("userId").is(userId))
                .with(Sort.by(Sort.Order.desc("created")))
                .skip((page - 1) * pagesize)
                .limit(pagesize);
        List<Friend> friends = mongoTemplate.find(query, Friend.class);
        return friends;
    }
}
