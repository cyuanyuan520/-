package com.tanhua.dubbo.api;

import com.tanhua.model.vo.PageResult;

public interface AnnouncementApi {
    PageResult getAnnouncements(Integer page, Integer pagesize);
}
