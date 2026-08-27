package org.dromara.walking.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 健步走作弊处理审计
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("walking_cheat_log")
public class WalkingCheatLog extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 会员id */
    private Long memberId;

    /** 活动id */
    private Long activityId;

    /** 异常数据日期 */
    private Date recordDate;

    /** 异常类型 */
    private String abnormalType;

    /** 处理方式(1标记 2删数据 3停用 4取消) */
    private Integer handleType;

    /** 处理管理员 */
    private String operator;

    /** 备注 */
    private String remark;

    @TableLogic
    private Long delFlag;
}
