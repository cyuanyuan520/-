package com.tanhua.server.controller;

import com.tanhua.dubbo.api.UserApi;
import com.tanhua.model.domain.Settings;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.SettingsVo;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.service.SettingsService;
import com.tanhua.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.mongo.ReactiveStreamsMongoClientDependsOnBeanFactoryPostProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/users")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;
    @Autowired
    private UserApi userApi;
    @Autowired
    private UserService userService;

    /**
     * 读取设置和陌生人问题
     * @return
     */
    @GetMapping("/settings")
    public ResponseEntity settings() {
        Long userId = UserHolder.getUserId();
        SettingsVo settingsVo = settingsService.settings();
        return ResponseEntity.ok(settingsVo);
    }

    /**
     * 修改陌生人问题
     * @param map
     * @return
     */
    @PostMapping("/questions")
    public ResponseEntity questions(@RequestBody Map map) {
        String question = (String) map.get("content");
        settingsService.questions(question);//调用settingsService处理question(string类型)
        return ResponseEntity.ok(null);
    }

    /**
     * 修改关于通知的3个设置
     * @return
     */
    @PostMapping("/notifications/setting")
    public ResponseEntity notifiSettings(@RequestBody Settings settings) {
        settingsService.notifiSettings(settings);//调用settingsService处理settings(settings类型)
        //返回数据
        return ResponseEntity.ok(null);
    }


    /**
     * 分页查询黑名单
     */
    @GetMapping("/blacklist")
    public ResponseEntity blacklist(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "1") int pagesize) {
        PageResult pageResult = settingsService.blacklist(page, pagesize);
        //返回数据
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 移除黑名单
     */
    @DeleteMapping("/blacklist/{uid}")
    public ResponseEntity DelBlacklist(@PathVariable("uid") long uid) {
        settingsService.delBlackList(uid);
        return ResponseEntity.ok(null);
    }

    /**
     * 修改手机号码:发送短信验证码
     */
    @PostMapping("/phone/sendVerificationCode")
    public ResponseEntity sendVerificationCode() {
        settingsService.sendVerificationCode();
        return ResponseEntity.ok(null);
    }

    /**
     * 修改手机号码:校验验证码
     */
    @PostMapping("/phone/checkVerificationCode")
    public ResponseEntity checkVerificationCode(@RequestBody Map map){
        String code = (String) map.get("verificationCode");
        boolean result = settingsService.verificationCode(code);
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("verification", result);
        return ResponseEntity.ok(resultMap);
    }

    /**
     * 修改手机号码:保存新号码
     * (传入00482734085即可)
     * 最后dubbo 的api层再修改
     */
    @PostMapping("/phone")
    public ResponseEntity phone(@RequestBody Map map){
        String phone = (String) map.get("phone");
        log.info("有用户正在请求把号码改成{}", phone);
        settingsService.changeMob(phone);
        return ResponseEntity.ok(null);
    }

}
