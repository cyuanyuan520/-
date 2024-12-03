package com.tanhua.server.controller;

import com.tanhua.commons.utils.Constants;
import com.tanhua.model.vo.HuanXinUserVo;
import com.tanhua.server.interceptor.UserHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/huanxin")
public class HuanXinController {

    /**
     * 查询用户环信账号密码
     */
    @GetMapping("/user")
    public ResponseEntity user() {
        HuanXinUserVo huanXinUserVo = new HuanXinUserVo("hx" + UserHolder.getUserId(), Constants.INIT_PASSWORD);
        return ResponseEntity.ok(huanXinUserVo);
    }



}
