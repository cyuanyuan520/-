package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.dubbo.mappers.UserMapper;
import com.tanhua.model.domain.User;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class UserApiImpl implements UserApi {

    @Autowired
    private UserMapper userMapper;

    /**
     * 根据联系方式查询用户
     * 需要传入用户输入的完整号码例如 (00482734085)
     * @param mobile
     * @return
     */
    @Override
    public User findUserByMobile(String mobile) {
        mobile = mobile.replaceFirst("^0+", "");//取到实际上注册人的联系方式
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile", mobile);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 在MySQL数据库添加用户
     * 需要传入已经修改好的mobile如(482734085)
     * @param user
     * @return
     */
    @Override
    public Long save(User user) {
        userMapper.insert(user);
        return user.getId();
    }

    /**
     * 根据id获取用户手机号
     * @param userId
     * @return
     */
    @Override
    public String getMobileById(Long userId) {
        User user = userMapper.selectById(userId);
        return user.getMobile();
    }

    /**
     * 修改密保手机
     * (传入00482734085即可)
     * @param userId
     * @param phone
     */
    @Override
    public void changeMob(Long userId, String phone) {
        User user = userMapper.selectById(userId);
        user.setMobile(phone.replaceFirst("^0+", ""));
        userMapper.updateById(user);
    }

    /**
     * 根据id更新User数据
     * @param user
     */
    @Override
    public void update(User user) {
        userMapper.updateById(user);
    }

    /**
     * 根据环信id查找User
     * @param huanxinId
     * @return
     */
    @Override
    public User findUserByHxId(String huanxinId) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("hx_user", huanxinId);
        User user = userMapper.selectOne(wrapper);
        return user;
    }

    /**
     * 根据id查询用户
     * @param aLong
     * @return
     */
    @Override
    public User findById(Long aLong) {
        User user = userMapper.selectById(aLong);
        return user;
    }


}
