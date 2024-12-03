package com.tanhua.server.controller;

import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.UserInfoVo;
import com.tanhua.server.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

@RestController
@RequestMapping("/messages")
public class MessagesController {

    @Autowired
    private MessageService messageService;

    /**
     * 根据环信用户ID查询用户信息
     */
    @GetMapping("/userinfo")
    public ResponseEntity userInfo(String huanxinId) throws InvocationTargetException, IllegalAccessException {
        UserInfoVo vo = messageService.findUserinfoByHxId(huanxinId);
        return ResponseEntity.ok(vo);
    }

    /**
     * 添加好友
     */
    @PostMapping("/contacts")
    public ResponseEntity contacts(@RequestBody Map map) {
        Long userId = Long.valueOf(map.get("userId").toString());//friendId
        messageService.addContact(userId);
        return ResponseEntity.ok(null);
    }

    /**
     * 好友列表
     */
    @GetMapping("/contacts")
    public ResponseEntity getContacts(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer pagesize, @RequestParam(required = false) String keyword){
        PageResult pr = messageService.getContacts(page, pagesize, keyword);
        return ResponseEntity.ok(pr);
    }

    /**
     * 查看自己的点赞列表
     */
    @GetMapping("/likes")
    public ResponseEntity getLikesRecords(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer pagesize){
        //调用service
        PageResult pr = messageService.getLikesRecords(page, pagesize);
        //返回
        return ResponseEntity.ok(pr);
    }

    /**
     * 查看自己的评论列表
     */
    @GetMapping("/comments")
    public ResponseEntity getCommentsRecords(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer pagesize){
        //调用service
        PageResult pr = messageService.getCommentsRecords(page, pagesize);
        //返回
        return ResponseEntity.ok(pr);
    }

    /**
     * 查看自己的被收藏列表
     */
    @GetMapping("/loves")
    public ResponseEntity getLovesRecords(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer pagesize){
        //调用service
        PageResult pr = messageService.getLovesRecords(page, pagesize);
        //返回
        return ResponseEntity.ok(pr);
    }

    /**
     * 查看公告
     */
    @GetMapping("/announcements")
    public ResponseEntity getAnnouncements(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult pr = messageService.getAnnouncements(page, pagesize);
        return ResponseEntity.ok(pr);
    }


}
