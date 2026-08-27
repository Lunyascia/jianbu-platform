package org.dromara.walking.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.sql.Date;

/**
 * 健步走步数打卡
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("walking_step_record")
public class WalkingStepRecord extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 会员id */
    private Long memberId;

    /** 活动id */
    private Long activityId;

    /** 打卡日期 */
    private Date recordDate;

    /** 步数 */
    private Integer steps;

    /** 来源(1微信运动 2人工) */
    private Integer source;

    /** 异常标志(0正常 1异常) */
    private Integer abnormalFlag;

    /** 是否锁定(0未锁定 1已锁定,历史固化) */
    private Integer locked;

    @TableLogic
    private Long delFlag;
}
