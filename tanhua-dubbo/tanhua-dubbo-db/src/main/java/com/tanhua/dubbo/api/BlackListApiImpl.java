package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.dubbo.mappers.BlackListMapper;
import com.tanhua.dubbo.mappers.UserInfoMapper;
import com.tanhua.model.domain.BlackList;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.vo.PageResult;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class BlackListApiImpl implements BlackListApi{

    @Autowired
    private BlackListMapper blackListMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;

    /**
     *分页查询黑名单列表
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public Page<UserInfo> blacklist(Long userId, int page, int pagesize) {
        Page pageInfo = new Page<>(page, pagesize);
        Page<UserInfo> blackList = userInfoMapper.getBlackListById(pageInfo, userId);
        return blackList;
    }

    /**
     * 移除黑名单
     * 需要传入操作人id 要移除的用户id
     * @param userId
     * @param uid
     */
    @Override
    public void delById(Long userId, long uid) {
        QueryWrapper<BlackList> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("black_user_id", uid);
        queryWrapper.eq("user_id", userId);
        blackListMapper.delete(queryWrapper);
    }
}
