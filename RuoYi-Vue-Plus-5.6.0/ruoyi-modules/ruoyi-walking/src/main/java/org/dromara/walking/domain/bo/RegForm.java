package org.dromara.walking.domain.bo;

import lombok.Data;

/**
 * 报名表单
 */
@Data
public class RegForm {
    /** 活动id（可选，默认当前活动） */
    private Long activityId;
    /** 单位id */
    private Long deptId;
    /** 单位名称 */
    private String deptName;
    /** 姓名 */
    private String realName;
    /** 手机号 */
    private String phone;
    /** 身份证号（选填） */
    private String idCard;
}
