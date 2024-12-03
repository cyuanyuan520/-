package com.tanhua.dubbo.api;

import com.tanhua.model.enums.CommentType;
import com.tanhua.model.mongo.Comment;
import com.tanhua.model.vo.PageResult;

import java.util.List;

public interface CommentApi {
    Integer save(Comment comment);

    PageResult getComments(String movementId, CommentType commentType, Integer page, Integer pagesize);

    boolean hasComment(String movementId, Long userId, CommentType commentType);

    Integer deleteComment(Comment comment);

    Integer likeComment(String commentId);

    Integer dislikeComment(String commentId);

    List<Comment> getMyCommemts(Long publishId, CommentType commentType, Integer page, Integer pagesize);
}
