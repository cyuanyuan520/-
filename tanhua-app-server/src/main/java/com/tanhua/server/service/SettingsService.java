package com.tanhua.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.dubbo.api.*;
import com.tanhua.model.domain.BlackList;
import com.tanhua.model.domain.Question;
import com.tanhua.model.domain.Settings;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.SettingsVo;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SettingsService {

    @DubboReference
    private QuestionApi questionApi;
    @DubboReference
    private SettingsApi settingsApi;
    @DubboReference
    private BlackListApi blackListApi;
    @DubboReference
    private UserInfoApi userInfoApi;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private UserApi userApi;

    /**
     * 查询陌生人问题+用户后台设置+qq号
     * @return
     */
    public SettingsVo settings() {
        SettingsVo vo = new SettingsVo();
        //获取当前用户id及qq号
        Long userId = UserHolder.getUserId();
        String mobile = UserHolder.getMobile();
        vo.setId(userId);
        vo.setPhone(mobile);
        //获取陌生人问题
        Question question = questionApi.findByUserId(userId);
        String txt = question == null ? "你喜欢java吗?" : question.getTxt();
        vo.setStrangerQuestion(txt);
        //获取3个基础设置
        Settings settings = settingsApi.findByUserId(userId);
        vo.setLikeNotification(settings == null ? true : settings.getLikeNotification());
        vo.setPinglunNotification(settings == null ? true : settings.getPinglunNotification());
        vo.setGonggaoNotification(settings == null ? true : settings.getGonggaoNotification());
        return vo;
    }

    /**
     * 设置或更新陌生人问题
     */
    public void questions(String question) {
        //获取用户id
        Long userId = UserHolder.getUserId();
        //借助dubbo调用questionApi查询数据库
        Question res = questionApi.findByUserId(userId);//res是一个Question对象 下面可以直接用这个对象执行插入或者更新
        if (res == null) {
            //数据库中没有查询到数据 创建一条新的记录
            res = new Question();
            res.setTxt(question);
            res.setUserId(userId);
            questionApi.save(res);
        } else {
            //数据库中没有查询到了数据 更新这条数据即可
            res.setTxt(question);
            questionApi.update(res);
        }

    }


    /**
     * 更新三项基础配置
     */
    public void notifiSettings(Settings settings) {
        //获取用户id
        Long userId = UserHolder.getUserId();
        //借助dubbo调用settingsApi查询数据库
        Settings res = settingsApi.findByUserId(userId);//res是一个Settings对象 下面可以直接用这个对象执行插入或者更新
        if (res == null) {
            //数据库中没有查询到数据 创建一条新的记录
            settings.setUserId(userId);
            settingsApi.save(settings);
        } else {
            //数据库中查询到了数据 更新这条数据即可
            settings.setId(res.getId());
            settings.setUserId(userId);
            settingsApi.update(settings);
        }
    }


    /**
     * 分页展示黑名单
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult blacklist(int page, int pagesize) {
        //获取当前用户的id
        Long userId = UserHolder.getUserId();
        //使用dubbo远程调用blacklistApi
        Page<UserInfo> blackListPage = blackListApi.blacklist(userId, page, pagesize);
        //开始构造PageResult
        PageResult pageResult = new PageResult();
            //给最后返回的对象插入数据
        pageResult.setPages(blackListPage.getPages());
        pageResult.setCounts(blackListPage.getTotal());
        pageResult.setPagesize(pagesize);
        pageResult.setPage(page);
            //封装userINfo
        pageResult.setItems(blackListPage.getRecords());
        return pageResult;
    }

    public void delBlackList(long uid) {
        //获取当前操作用户id
        Long userId = UserHolder.getUserId();
        //dubbo调用blackListApi接口
        blackListApi.delById(userId, uid);
    }

    /**
     * 修改手机号码:发送短信验证码
     * 获取mobile这步必须从数据库读取 否则有可能会导致无法正确发送
     */
    public void sendVerificationCode() {
        Long userId = UserHolder.getUserId();
        String mobile = userApi.getMobileById(userId);
        userService.sendMsg(mobile);//同时已经保存到redis
    }

    /**
     * 校验验证码
     * 返回校验成功与否(boolean)
     * 获取mobile这步从数据库读取 否则有可能会导致无法正确发送
     * @return
     */
    public boolean verificationCode(String code) {
        Long userId = UserHolder.getUserId();
        String mobile = userApi.getMobileById(userId);
        //1.获取redis中的验证码
        String email = mobile.replaceFirst("^0+", "") + "@qq.com";
        String realCode = redisTemplate.opsForValue().get("CHECK_CODE_" + email);
        //2.校验验证码
        if (code == null || !realCode.equals(code)){
            //验证码无效
            return false;
        }else {
            //销毁redis中保存的验证码并返回
            redisTemplate.delete("CHECK_CODE_" + email);
        }
        return true;
    }

    public void changeMob(String phone) {
        //获取操作人id
        Long userId = UserHolder.getUserId();
        userApi.changeMob(userId, phone);
    }
}
