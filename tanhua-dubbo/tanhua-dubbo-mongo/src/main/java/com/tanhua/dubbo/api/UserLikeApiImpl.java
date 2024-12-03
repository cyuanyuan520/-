package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.UserLike;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@Slf4j
@DubboService
public class UserLikeApiImpl implements UserLikeApi {

    @Autowired
    private MongoTemplate mongoTemplate;


    /**
     *  操作user_like表
     *  向里面添加喜欢或不喜欢的数据
     */
    @Override
    public Boolean saveOrUpdate(Long userId, long likeUserId, boolean likeOrNot) {
        try {
            //原先是否有数据存在?
            Query query = new Query(Criteria.where("userId").is(userId).and("likeUserId").is(likeUserId));
            boolean exists = mongoTemplate.exists(query, UserLike.class);
            //有就更新
            if (exists) {
                Update update = Update.update("isLike", likeOrNot)
                        .set("updated", System.currentTimeMillis());
                mongoTemplate.updateFirst(query, update, UserLike.class);
            } else {
                //没有就插入
                UserLike userLike = new UserLike();
                userLike.setUserId(userId);
                userLike.setLikeUserId(likeUserId);
                userLike.setIsLike(likeOrNot);
                userLike.setCreated(System.currentTimeMillis());
                userLike.setUpdated(System.currentTimeMillis());
                mongoTemplate.save(userLike);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
