package com.tanhua.dubbo.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.model.domain.UserInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserInfoMapper extends BaseMapper<UserInfo> {

    @Select("SELECT * FROM tb_user_info WHERE id IN ( SELECT black_user_id FROM tb_black_list WHERE user_id = #{userId} )")
    Page<UserInfo> getBlackListById(@Param("pageInfo") Page pageInfo, @Param("userId") Long userId);
}
