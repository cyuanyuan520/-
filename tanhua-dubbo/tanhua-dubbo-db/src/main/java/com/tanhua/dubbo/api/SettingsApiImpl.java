package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.dubbo.mappers.SettingsMapper;
import com.tanhua.model.domain.Settings;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.PrivateKey;

@DubboService
public class SettingsApiImpl implements SettingsApi{

    @Autowired
    private SettingsMapper settingsMapper;

    /**
     * 查询陌生人问题
     * @param userId
     * @return
     */
    @Override
    public Settings findByUserId(Long userId) {
        QueryWrapper<Settings> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);//限定用户id
        Settings settings = settingsMapper.selectOne(wrapper);
        return settings;
    }

    /**
     * 第一次设置陌生人问题
     * @param res
     */
    @Override
    public void save(Settings res) {
        settingsMapper.insert(res);
    }

    /**
     * 更新陌生人问题
     * @param res
     */
    @Override
    public void update(Settings res) {
        settingsMapper.updateById(res);
    }

}
