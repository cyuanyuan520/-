package com.tanhua.server.controller;

import com.tanhua.model.vo.PageResult;
import com.tanhua.server.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 发布评论
     */
    @PostMapping
    public ResponseEntity addComment(@RequestBody Map map) {
        String movementId = (String) map.get("movementId");//评论的动态id
        String content = (String) map.get("comment");//评论内容
        commentService.saveComment(movementId, content);
        return ResponseEntity.ok(null);
    }

    /**
     * 查询评论列表
     */
    @GetMapping
    public ResponseEntity getComments(String movementId, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult pr = commentService.getComments(movementId, page, pagesize);
        return ResponseEntity.ok(pr);
    }

    /**
     * 给动态底下的评论点赞
     */
    @GetMapping("/{id}/like")
    public ResponseEntity likeComment(@PathVariable String id) {
        Integer count = commentService.likeComment(id);
        return ResponseEntity.ok(count);
    }

    /**
     * 给动态底下的评论取消点赞
     */
    @GetMapping("/{id}/dislike")
    public ResponseEntity dislikeComment(@PathVariable String id) {
        Integer count = commentService.dislikeComment(id);
        return ResponseEntity.ok(count);
    }


}
