package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Comment;
import com.tanhua.model.mongo.Video;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collections;
import java.util.List;

@DubboService
public class VideoCommentApiImpl implements VideoCommentApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 保存互动数据
     *
     * @param comment
     */
    @Override
    public void save(Comment comment) {
        //检查是否出现重复点赞情况
        Query query = new Query(Criteria.where("publishId").is(comment.getPublishId())
                .and("commentType").is(1)
                .and("userId").is(comment.getUserId()));
        //判断是否存在相同点赞数据(可以发重复评论)
        if (comment.getCommentType() == 1 && mongoTemplate.exists(query, Comment.class)) {
            //同时也要确保用户现在发的还是点赞数据
            //否则可能会导致用户发布完点赞后无法评论
            return;
        }

        //封装上publishUserId
        Query queryUser = new Query(Criteria.where("id").is(comment.getPublishId()));
        Video video = mongoTemplate.findOne(queryUser, Video.class);
        comment.setPublishUserId(video.getUserId());
        //保存互动数据
        mongoTemplate.save(comment);
        //视频互动数+1
        Query queryVideo = new Query(Criteria.where("id").is(comment.getPublishId()));
        Update update = new Update();
        //根据互动类型不同 增加不同数据
        if (comment.getCommentType() == 1) {
            update.inc("likeCount", 1);
        } else if (comment.getCommentType() == 2) {
            update.inc("commentCount", 1);
        }
        mongoTemplate.updateFirst(queryVideo,
                update,
                Video.class);
    }

    /**
     * 取消点赞(不能删除评论)
     *
     * @param comment
     */
    @Override
    public void disLike(Comment comment) {
        //删除数据
        Query query = new Query(Criteria.where("publishId").is(comment.getPublishId())
                .and("commentType").is(1)
                .and("userId").is(comment.getUserId()));
        mongoTemplate.remove(query, Comment.class);
        //video表点赞数-1
        Query queryVideo = new Query(Criteria.where("id").is(comment.getPublishId()));
        mongoTemplate.updateFirst(queryVideo,
                new Update().inc("likeCount", -1),
                Video.class);
    }

    /**
     * 获取评论列表
     *
     * @param publishId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public List<Comment> getCommentList(String publishId, Integer page, Integer pagesize) {
        Query query = new Query(Criteria.where("publishId").is(new ObjectId(publishId))
                .and("commentType").is(2))
                .skip((page - 1) * pagesize)
                .limit(pagesize);
        List<Comment> comments = mongoTemplate.find(query, Comment.class);
        return comments;
    }

    /**
     * 视频评论点赞
     * likecount+1 不需要再保存一条互动数据
     *
     * @param id
     */
    @Override
    public void likeComment(String id) {
        Query query = new Query(Criteria.where("id").is(new ObjectId(id)));
        mongoTemplate.updateFirst(query,
                new Update().inc("likeCount", 1),
                Comment.class);
    }

    /**
     * 视频评论取消点赞
     * likecount-1 不需要再保存一条互动数据
     *
     * @param id
     */
    @Override
    public void dislikeComment(String id) {
        Query query = new Query(Criteria.where("id").is(new ObjectId(id)));
        mongoTemplate.updateFirst(query,
                new Update().inc("likeCount", -1),
                Comment.class);
    }

}
