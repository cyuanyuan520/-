package com.tanhua.dubbo.api;

import com.tanhua.model.enums.CommentType;
import com.tanhua.model.mongo.Comment;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.vo.PageResult;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collections;
import java.util.List;


@DubboService
public class CommentApiImpl implements CommentApi{

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 保存评论/like/lova
     * @param comment
     * @return
     */
    @Override
    public Integer save(Comment comment) {
        //填充publishUserId
        ObjectId movementId = comment.getPublishId();//动态的id
        Query query = new Query(Criteria.where("_id").is(movementId));
        Movement movement = mongoTemplate.findOne(query, Movement.class);
        if (movement != null) {
            comment.setPublishUserId(movement.getUserId());
        }
        //调用mongodb保存数据
        mongoTemplate.save(comment);
            //更新movement数据
        Update update = new Update();
        if (comment.getCommentType() == CommentType.COMMENT.getType()) {
            update.inc("commentCount", 1);
        } else if (comment.getCommentType() == CommentType.LOVE.getType()) {
            update.inc("loveCount", 1);
        } else {
            update.inc("likeCount", 1);
        }
            //设置更新参数
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);
        Movement modify = mongoTemplate.findAndModify(query, update, options, Movement.class);//返回了已经修改完毕的结果
        return modify.statisCount(comment.getCommentType());//返回最新修改后的数据
    }

    /**
     * 取消like/love
     */
    public Integer deleteComment(Comment comment) {
        //删除comment数据
        Query query = new Query(Criteria.where("publishId").is(comment.getPublishId())
                .and("commentType").is(comment.getCommentType())
                .and("userId").is(comment.getUserId()));
        mongoTemplate.remove(query, Comment.class);
        //对应的movement like love 或comment -1
        Update update = new Update();
        if (comment.getCommentType() == CommentType.COMMENT.getType()) {
            update.inc("commentCount", -1);
        } else if (comment.getCommentType() == CommentType.LOVE.getType()) {
            update.inc("loveCount", -1);
        } else {
            update.inc("likeCount", -1);
        }
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);
        Query modify = new Query(Criteria.where("_id").is(comment.getPublishId()));//寻找当前互动对应的动态
        Movement newMovement = mongoTemplate.findAndModify(modify, update, options, Movement.class);
        return newMovement.statisCount(comment.getCommentType());//返回更新后的数据
    }



    /**
     * 根据动态id获取全部评论
     */
    @Override
    public PageResult getComments(String movementId, CommentType commentType, Integer page, Integer pagesize) {
        Query query = new Query(Criteria.where("publishId").is(new ObjectId(movementId))
                .and("commentType").is(commentType.getType()))
                .skip((page - 1) * pagesize)
                .limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        List<Comment> comments = mongoTemplate.find(query, Comment.class);
        long count = mongoTemplate.count(query, Comment.class);
        PageResult pr = new PageResult(page, pagesize, count, comments);
        return pr;
    }


    /**
     *检查是否互动(like, comment, love)过
     */
    @Override
    public boolean hasComment(String movementId, Long userId, CommentType commentType) {
        Query query = new Query(Criteria.where("publishId").is(new ObjectId(movementId))
                .and("userId").is(userId)
                .and("commentType").is(commentType.getType()));
        boolean exists = mongoTemplate.exists(query, Comment.class);
        return exists;
    }


    /**
     * 点赞评论
     * @param commentId
     * @return
     */
    public Integer likeComment(String commentId) {
        //构造搜寻条件
        Query query = new Query(Criteria.where("_id").is(new ObjectId(commentId))
                .and("commentType").is(CommentType.COMMENT.getType()));
        //update
        Update update = new Update();
        update.inc("likeCount", 1);
        //更新参数
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);
        //执行更新
        Comment modify = mongoTemplate.findAndModify(query, update, options, Comment.class);
        return modify.getLikeCount();
    }

    @Override
    public Integer dislikeComment(String commentId) {
        //构造搜寻条件
        Query query = new Query(Criteria.where("_id").is(new ObjectId(commentId))
                .and("commentType").is(CommentType.COMMENT.getType()));
        //update
        Update update = new Update();
        update.inc("likeCount", -1);
        //更新参数
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);
        //执行更新
        Comment modify = mongoTemplate.findAndModify(query, update, options, Comment.class);
        return modify.getLikeCount();
    }

    /**
     * 根据发布人id, 评论类型 分页数据找到commentList
     * @return
     */
    @Override
    public List<Comment> getMyCommemts(Long publishId, CommentType commentType, Integer page, Integer pagesize) {
        Query query = new Query(Criteria.where("publishUserId").is(publishId)
                .and("commentType").is(commentType.getType())).limit(pagesize).skip((page - 1) * pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        List<Comment> comments = mongoTemplate.find(query, Comment.class);
        return comments;
    }


}
