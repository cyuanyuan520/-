package com.tanhua.dubbo.api;

import com.tanhua.model.domain.UserInfo;

import java.util.List;
import java.util.Map;

public interface UserInfoApi {

    /**
     * 新建用户信息
     * @param userInfo
     */
    public void save(UserInfo userInfo);

    /**
     * 修改用户信息
     * @param userInfo
     */
    public void update(UserInfo userInfo);

    /**
     * 查询用户信息
     * @param userID
     * @return
     */
    UserInfo findById(Long userID);

    Map<Long, UserInfo> getInfoByIds(List<Long> ids, UserInfo info);

}
