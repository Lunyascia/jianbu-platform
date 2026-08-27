package org.dromara.walking.domain.vo.admin;

import lombok.Data;

import java.util.Date;

/**
 * 报名审核日志（后台）
 */
@Data
public class AuditLogVo {

    private Long id;

    /** 报名id */
    private Long registrationId;

    /** 会员id */
    private Long memberId;

    /** 会员姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 审核动作(提交/自动审核通过/自动审核拒绝/取消/撤下/停用) */
    private String auditAction;

    /** 审核结果/说明 */
    private String auditResult;

    /** 审核人(系统或管理员) */
    private String auditor;

    /** 审核时间 */
    private Date createTime;
}
