package org.dromara.walking.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 健步走积分
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("walking_points")
public class WalkingPoints extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 会员id */
    private Long memberId;

    /** 活动id */
    private Long activityId;

    /** 积分变动 */
    private Integer points;

    /** 积分来源 */
    private String reason;

    /** 发生日期 */
    private Date recordDate;

    @TableLogic
    private Long delFlag;
}
