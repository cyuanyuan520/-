package com.tanhua.server.controller;

import com.tanhua.commons.utils.JwtUtils;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.service.UserInfoService;
import com.tanhua.server.service.UserService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private UserService userService;

    /**
     * 保存用户信息
     */
    @PostMapping("/loginReginfo")
    public ResponseEntity loginReginfo(@RequestBody UserInfo userInfo, @RequestHeader("Authorization")String token) {

        //向UserInfo中设置id(userInfo表的id需要和user表的id保持一致)
        Long id = UserHolder.getUserId();
        userInfo.setId(Long.valueOf(id));
        userInfo.setAge(18);
        //调用service保存userInfo
        userInfoService.save(userInfo);
        return ResponseEntity.ok(null);
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/loginReginfo/head")
    public ResponseEntity loginReginfoHead(MultipartFile headPhoto, @RequestHeader("Authorization")String token) throws IOException {

        //2.给userInfo设置用户id
        Long id = UserHolder.getUserId();
        //调用service
        userInfoService.updateHead(headPhoto, id);
        //返回
        return ResponseEntity.ok(null);
    }




}
