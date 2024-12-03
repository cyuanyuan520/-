package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.model.domain.BlackList;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.vo.PageResult;

public interface BlackListApi {
    /**
     * 分页查询黑名单
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    Page<UserInfo> blacklist(Long userId, int page, int pagesize);

    /**
     * 移除黑名单的db接口
     * @param userId
     * @param uid
     */
    void delById(Long userId, long uid);
}
