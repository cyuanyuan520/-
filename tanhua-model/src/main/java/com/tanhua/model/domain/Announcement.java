package com.tanhua.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 公告实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Announcement extends BasePojo{
    private Long id;
    private String title;
    private String description;
}
