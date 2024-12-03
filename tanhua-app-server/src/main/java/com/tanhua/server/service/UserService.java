package com.tanhua.server.service;

import com.tanhua.autoconfig.properties.HuanXinProperties;
import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.autoconfig.template.SmsTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.commons.utils.JwtUtils;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.model.domain.User;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.server.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.joda.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserService {

    @Autowired
    private SmsTemplate smsTemplate;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private HuanXinTemplate huanXinTemplate;

    @DubboReference
    private UserApi userApi;
    /**
     * 发送验证码
     * @param phone
     */
    public void sendMsg(String phone) {
        //1.随机生成6位数字
        String code = RandomStringUtils.randomNumeric(6);
        //2.获取电子邮箱
        String email = phone.replaceFirst("^0+", "") + "@qq.com";
        log.info("{}正在申请验证码:{}",email, code);
        //3.发送邮件
//        smsTemplate.sendMessage(email, code); //暂时关闭发送验证码....
        //4.将验证码保存到redis
        log.info("正在使用redis缓存验证码");
        redisTemplate.opsForValue().set("CHECK_CODE_" + email, code, 5L, TimeUnit.MINUTES);
        log.info("redis存储执行完毕");
    }

    /**
     * 验证码
     * @param phone
     * @param code
     * @return
     */
    public Map loginVerification(String phone, String code){
        //0.获取真实号码
        String email = phone.replaceFirst("^0+", "") + "@qq.com";
        //1.获取redis中的验证码
        String realCode = redisTemplate.opsForValue().get("CHECK_CODE_" + email);
        //2.校验验证码
        if (code == null || !realCode.equals(code)){
            //验证码无效
            throw new BusinessException(ErrorResult.loginError());//自定义异常
        }
        //3.删除redis中的验证码(验证码校验完应该失效)
        redisTemplate.delete("CHECK_CODE_" + email);
        //4.通过手机号查询用户
        User user = userApi.findUserByMobile(phone);
        boolean isNew = false;
        //5.如果是新用户: 创建并保存到数据库
        if (user == null) {
            user = new User();
            user.setMobile(phone.replaceFirst("^0+", ""));//已经修改好 如482734085
            user.setPassword(DigestUtils.md5Hex("123456"));
            Long id = userApi.save(user);
            user.setId(id);
            isNew = true;

            //注册环信账户
            String hxUsername = "hx" + user.getId();
            Boolean res = huanXinTemplate.createUser(hxUsername, Constants.INIT_PASSWORD);
            if (res) {
                user.setHxUser(hxUsername);
                user.setHxPassword(Constants.INIT_PASSWORD);
                userApi.update(user);
            }
        }
        //6.jwt生成token(id和手机号码)
        Map tokenMap = new HashMap();
        tokenMap.put("id", user.getId());
        tokenMap.put("mobile", phone.replaceFirst("^0+", ""));//token里存储的是处理后的手机号码如(482734085)
        String token = JwtUtils.getToken(tokenMap);
        //7.构造返回值
        Map resuleMap = new HashMap();
        resuleMap.put("token", token);
        resuleMap.put("isNew", isNew);
        return resuleMap;
    }

}
