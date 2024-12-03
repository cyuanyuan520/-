package com.tanhua.dubbo.api;

import com.tanhua.model.domain.User;

public interface UserApi {

    //根据手机号码查询用户
    User findUserByMobile(String mobile);

    Long save(User user);

    String getMobileById(Long userId);
    //修改密保手机
    void changeMob(Long userId, String phone);

    void update(User user);

    User findUserByHxId(String huanxinId);

    User findById(Long aLong);
}
