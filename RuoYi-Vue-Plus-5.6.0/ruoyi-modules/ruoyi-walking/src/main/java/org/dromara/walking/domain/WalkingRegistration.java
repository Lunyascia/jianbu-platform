package org.dromara.walking.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 健步走报名
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("walking_registration")
public class WalkingRegistration extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 会员id */
    private Long memberId;

    /** 活动id */
    private Long activityId;

    /** 状态(0待提交草稿 1待审核 2审核通过 3已取消 4已停用) */
    private Integer status;

    /** 审核结果/异常原因 */
    private String auditResult;

    /** 提交时间 */
    private Date submitTime;

    /** 审核时间 */
    private Date auditTime;

    /** 取消/撤下操作人 */
    private String cancelBy;

    /** 取消/撤下时间 */
    private Date cancelTime;

    @TableLogic
    private Long delFlag;
}
