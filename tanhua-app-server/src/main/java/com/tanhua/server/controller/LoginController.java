package com.tanhua.server.controller;


import com.tanhua.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * 发送验证码
     * @param map
     */
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody Map map){
        String phone = (String) map.get("phone");
        userService.sendMsg(phone);
        return ResponseEntity.ok(null);
    }

    /**
     * 校验验证码
     * /user/loginVerification
     * phone
     * verificationCode
     */
    @PostMapping("/loginVerification")
    public ResponseEntity loginVerification(@RequestBody Map map) {
        //1.调用map集合获取参数
        String phone = (String) map.get("phone");//得到联系方式
        String code = (String) map.get("verificationCode");//得到用户输入的验证码
        //2.调用userservice进行校验
        Map retMap = userService.loginVerification(phone, code);
        return ResponseEntity.ok(retMap);
    }

}
