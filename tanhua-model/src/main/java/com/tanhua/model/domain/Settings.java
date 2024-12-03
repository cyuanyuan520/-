package com.tanhua.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知设置表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Settings extends BasePojo {

    private Long id;
    private Long userId;
    private Boolean likeNotification;//推送喜欢通知
    private Boolean pinglunNotification;//推送评论通知
    private Boolean gonggaoNotification;//推送公告通知

}