package com.tanhua.model.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public abstract class BasePojo implements Serializable {
    @TableField(fill = FieldFill.INSERT)//当向数据库插入数据时自动填充
    private Date created;

    @TableField(fill = FieldFill.INSERT_UPDATE)//当向数据库插入数据_更新数据时自动填充
    private Date updated;
}
