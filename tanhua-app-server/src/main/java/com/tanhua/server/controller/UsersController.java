package com.tanhua.server.controller;

import com.tanhua.commons.utils.JwtUtils;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.vo.UserInfoVo;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.service.UserInfoService;
import io.jsonwebtoken.Claims;
import net.sf.jsqlparser.expression.LongValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/users")
public class UsersController {

    private static final Logger log = LoggerFactory.getLogger(UsersController.class);
    @Autowired
    private UserInfoService userInfoService;


    /**
     * 查询用户信息
     * @param token
     * @param userID
     * @return
     */
    @GetMapping
    public ResponseEntity users(@RequestHeader("Authorization") String token, Long userID) {

        //判断是否传入userID参数
        if (userID == null) {
            //获取token中的用户id
            Long id = UserHolder.getUserId();
            //将userID设置为token中的id
            userID = Long.valueOf(id);
        }
        //调用UserInfoService查询数据
        UserInfoVo userInfoVo = userInfoService.fingById(userID);
        //返回数据
        return ResponseEntity.ok(userInfoVo);
    }

    /**
     * 更新用户信息(不包括头像)
     * @param userInfo
     * @param token
     * @return
     */
    @PutMapping
    public ResponseEntity users(@RequestBody UserInfo userInfo, @RequestHeader("Authorization") String token) {

        //获取用户id
        Long id = UserHolder.getUserId();
        //在userInfo中存入用户id
        userInfo.setId(Long.valueOf(id));
        //调用userInfoService
        userInfoService.update(userInfo);
        //返回
        return ResponseEntity.ok(null);
    }

    /**
     * 更新头像(不是初次设置头像)
     * @RequestBody 不适用于文件上传，因为文件上传通常是通过 multipart/form-data 格式提交的，而不是 JSON 或 XML 格式
     * 所以直接使用MultipartFile来接 确保前后端一致就可以了
     */
    @PostMapping("/header")
    public ResponseEntity updateHead(MultipartFile headPhoto) throws IOException {
        if (headPhoto == null) {
            System.out.println("你上传了一张空的图片");
        } else {
            System.out.println("你上传的图片不是空的!");
        }
        //在threadLocal中获取当前登录的用户id
        Long userId = UserHolder.getUserId();
        //调用userInfoService存储图片
        userInfoService.updateHead(headPhoto, userId);
        return ResponseEntity.ok(null);
    }


}
