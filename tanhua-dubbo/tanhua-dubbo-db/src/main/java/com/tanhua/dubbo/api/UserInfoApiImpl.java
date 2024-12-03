package com.tanhua.dubbo.api;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.dubbo.mappers.UserInfoMapper;
import com.tanhua.model.domain.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@DubboService
@Slf4j
public class UserInfoApiImpl implements UserInfoApi{

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public void save(UserInfo userInfo) {
        userInfoMapper.insert(userInfo);
    }

    @Override
    public void update(UserInfo userInfo) {
        userInfoMapper.updateById(userInfo);
    }

    @Override
    public UserInfo findById(Long userID) {
        UserInfo userInfo = userInfoMapper.selectById(userID);
        log.error("查询出的userInfo: " + userInfo);
        return userInfo;
    }

    /**
     * 根据ids批量条件查询
     * 第二个参数是额外条件(age<=35 sex=girl)
     * 不需要再加额外条件可以输入null
     * @param ids
     * @param info
     * @return
     */
    @Override
    public Map<Long, UserInfo> getInfoByIds(List<Long> ids, UserInfo info) {
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.in("id", ids);
        //如果集合为空 就不做查询 以免mysql报错
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        //附加条件(可加可不加)
        if (info != null) {
            //如果info不为空再进行接下来的操作 防止空指针
            if (info.getAge() != null) {
                //age不为空
                wrapper.lt("age", info.getAge());
            }
            if (info.getGender() != null) {
                //性别不为空
                wrapper.eq("gender", info.getGender());
            }
            if (info.getNickname() != null) {
                //昵称不为空
                wrapper.like("nickname", info.getNickname());
            }
        }
        List<UserInfo> userInfos = userInfoMapper.selectList(wrapper);
        Map<Long, UserInfo> idMap = CollUtil.fieldValueMap(userInfos, "id");
        return idMap;
    }



}
