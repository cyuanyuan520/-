package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Visitors;

import java.util.List;

public interface VisitorsApi {

    /**
     * 保存访客记录:mongodb
     */
    void save(Visitors visitor);

    List<Visitors> datedVisitors(Long date, Long userId, Integer page, Integer pagesize);
}
