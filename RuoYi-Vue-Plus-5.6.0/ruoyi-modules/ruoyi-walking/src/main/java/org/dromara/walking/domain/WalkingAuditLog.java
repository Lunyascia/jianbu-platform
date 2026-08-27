package org.dromara.walking.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 健步走报名审核日志
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("walking_audit_log")
public class WalkingAuditLog extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 报名id */
    private Long registrationId;

    /** 会员id */
    private Long memberId;

    /** 审核动作(提交/自动审核通过/自动审核拒绝/取消/撤下/停用) */
    private String auditAction;

    /** 审核结果/说明 */
    private String auditResult;

    /** 审核人(系统或管理员) */
    private String auditor;

    @TableLogic
    private Long delFlag;
}
