package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.dubbo.mappers.AnnouncementMapper;
import com.tanhua.model.domain.Announcement;
import com.tanhua.model.vo.AnnouncementVo;
import com.tanhua.model.vo.PageResult;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@DubboService
public class AnnouncementApiImpl implements AnnouncementApi {
    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public PageResult getAnnouncements(Integer page, Integer pagesize) {
        //构造分页查询条件
        Page<Announcement> announcementPage = new Page<>(page, pagesize);
        IPage<Announcement> pageInfo = announcementMapper.selectPage(announcementPage, null);
        List<Announcement> announcements = pageInfo.getRecords();
        //构造pageResult(vo)
        PageResult pageResult = new PageResult(page, pagesize, (long) announcements.size(), announcements);
        List<Announcement> items = (List<Announcement>) pageResult.getItems();
        ArrayList<AnnouncementVo> vos = new ArrayList<>();
        for (Announcement item : items) {
            AnnouncementVo vo = AnnouncementVo.init(item);
            vos.add(vo);
        }
        pageResult.setItems(vos);
        return pageResult;
    }
}
