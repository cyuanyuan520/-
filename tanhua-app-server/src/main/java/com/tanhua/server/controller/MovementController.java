package com.tanhua.server.controller;

import com.tanhua.dubbo.api.CommentApi;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.vo.MovementsVo;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.VisitorsVo;
import com.tanhua.server.service.CommentService;
import com.tanhua.server.service.MovementService;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/movements")
public class MovementController {

    @Autowired
    private MovementService movementService;
    @Autowired
    private CommentService commentService;

    /**
     * 发布动态
     */
    @PostMapping
    public ResponseEntity PostMovements(Movement movement, MultipartFile[] imageContent) throws IOException {
        movementService.publishMovement(movement, imageContent);
        return ResponseEntity.ok(null);
    }

    /**
     * 我的动态
     */
    @GetMapping("/all")
    public ResponseEntity getMyMovement(Long userId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize) {
        PageResult pr = movementService.getMovementById(userId, page, pagesize);
        return ResponseEntity.ok(pr);
    }

    /**
     * 好友动态
     */
    @GetMapping
     public ResponseEntity getFriendMovements(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize) {
        PageResult pr = movementService.getFriendMovements(page, pagesize);
        return ResponseEntity.ok(pr);
    }

    /**
     * 推荐动态
     */
    @GetMapping("/recommend")
    public ResponseEntity getRecommandMovements(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize) {
        PageResult pr = movementService.getRecommandMovements(page, pagesize);
        return ResponseEntity.ok(pr);
    }

    /**
     * 查询单条动态详情
     */
    @GetMapping("/{id}")
    public ResponseEntity getMovementById(@PathVariable("id") String movementId) {
        MovementsVo movementsVo = movementService.getMovementByMId(movementId);
        return ResponseEntity.ok(movementsVo);
    }

    /**
     * 点赞动态
     */
    @GetMapping("/{id}/like")
    public ResponseEntity like(@PathVariable("id") String movementId) {
        Integer likeCount = commentService.like(movementId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * 取消点赞(like)动态
     */
    @GetMapping("/{id}/dislike")
    public ResponseEntity dislike(@PathVariable("id") String movementId) {
        Integer likeCount = commentService.disLike(movementId);//调用service取消点赞
        return ResponseEntity.ok(likeCount);//返回取消点赞后like的数量
    }

    /**
     * 收藏动态
     */
    @GetMapping("/{id}/love")
    public ResponseEntity love(@PathVariable("id") String movementId) {
        Integer likeCount = commentService.love(movementId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * 取消收藏(love)动态
     */
    @GetMapping("/{id}/unlove")
    public ResponseEntity unLove(@PathVariable("id") String movementId) {
        Integer likeCount = commentService.unLove(movementId);//调用service取消收藏
        return ResponseEntity.ok(likeCount);//返回取消点赞后like的数量
    }

    /**
     * 首页访客列表(头像)
     */
    @GetMapping("visitors")
    public ResponseEntity visitors() {
        List<VisitorsVo> vos = movementService.visitors();
        return ResponseEntity.ok(vos);
    }


}
