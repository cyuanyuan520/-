package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Comment;

import java.util.List;

public interface VideoCommentApi {
    void save(Comment comment);

    void disLike(Comment comment);

    List<Comment> getCommentList(String id, Integer page, Integer pagesize);

    void likeComment(String id);

    void dislikeComment(String id);
}
