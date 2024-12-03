package com.tanhua.server.controller;

import com.tanhua.model.vo.PageResult;
import com.tanhua.server.service.SmallVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.util.resources.cldr.lu.CurrencyNames_lu;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/smallVideos")
public class SmallVideoController {

    @Autowired
    private SmallVideoService smallVideoService;

    /**
     * 上传视频
     */
    @PostMapping
    public ResponseEntity uploadVideo(MultipartFile videoThumbnail, MultipartFile videoFile) throws IOException {
        smallVideoService.uploadVideo(videoThumbnail, videoFile);
        return ResponseEntity.ok(null);
    }

    /**
     * 查询视频列表
     */
    @GetMapping
    public ResponseEntity listVideo(@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult pr = smallVideoService.getVideoList(page, pagesize);
        return ResponseEntity.ok(pr);
    }

    /**
     * 关注视频作者
     * 传过来的是作者id
     */
    @PostMapping("/{uid}/userFocus")
    public ResponseEntity follow(@PathVariable long uid) {
        smallVideoService.follow(uid);
        return ResponseEntity.ok(null);
    }

    /**
     * 取关视频作者
     * 传过来的是作者id
     */
    @PostMapping("/{uid}/userUnFocus")
    public ResponseEntity unFollow(@PathVariable long uid) {
        smallVideoService.unFollow(uid);
        return ResponseEntity.ok(null);
    }

    /**
     * 视频点赞
     * 传入视频id
     */
    @PostMapping("/{id}/like")
    public ResponseEntity like(@PathVariable String id) {
        smallVideoService.likeVideo(id);
        return ResponseEntity.ok(null);
    }

    /**
     * 视频取消点赞
     * 传入视频id
     */
    @PostMapping("/{id}/dislike")
    public ResponseEntity dislike(@PathVariable String id) {
        smallVideoService.dislikeVideo(id);
        return ResponseEntity.ok(null);
    }

    /**
     * 发布视频评论
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity comment(@PathVariable String id, @RequestBody Map map) {
        //获取评论内容
        String comment = (String) map.get("comment");
        smallVideoService.addComment(id, comment);
        return ResponseEntity.ok(null);
    }

    /**
     * 查询评论列表
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity getComment(@PathVariable String id, @RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult pr = smallVideoService.getComment(id, page, pagesize);
        return ResponseEntity.ok(pr);
    }

    /**
     * 点赞视频下方评论
     * 传入评论id
     */
    @PostMapping("/comments/{id}/like")
    public ResponseEntity likeComment(@PathVariable String id) {
        smallVideoService.likeComment(id);
        return ResponseEntity.ok(null);
    }


    /**
     * 取消点赞视频下方评论
     * 传入评论id
     */
    @PostMapping("/comments/{id}/dislike")
    public ResponseEntity dislikeComment(@PathVariable String id) {
        smallVideoService.dislikeComment(id);
        return ResponseEntity.ok(null);
    }






}
