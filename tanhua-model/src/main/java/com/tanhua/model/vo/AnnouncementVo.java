package com.tanhua.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.tanhua.model.domain.Announcement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementVo implements Serializable {

    private Long id;
    private String title;
    private String description;
    private Date createDate;

    /**
     * 自动化生产vo
     * @param announcement
     * @return
     */
    public static AnnouncementVo init(Announcement announcement) {
        AnnouncementVo vo = new AnnouncementVo();
        BeanUtils.copyProperties(announcement, vo);
        vo.setCreateDate(announcement.getCreated());
        return vo;
    }

}
