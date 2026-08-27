package org.dromara.walking.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 健步走活动
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("walking_activity")
public class WalkingActivity extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 活动名称 */
    private String activityName;

    /** 封面图 */
    private String coverUrl;

    /** 活动介绍 */
    private String description;

    /** 活动规则 */
    private String ruleContent;

    /** 开始日期 */
    private Date startDate;

    /** 结束日期 */
    private Date endDate;

    /** 目标总步数 */
    private Integer targetSteps;

    /** 每日目标步数 */
    private Integer dailyTargetSteps;

    /** 每千步积分 */
    private Integer pointsPerThousandSteps;

    /** 单日步数上限(超出不计入统计) */
    private Integer dailyStepLimit;

    /** 连续7天奖励分 */
    private Integer streak7Points;

    /** 连续14天奖励分 */
    private Integer streak14Points;

    /** 全程全勤奖励分 */
    private Integer fullAttendancePoints;

    /** 缓冲期时长(天,活动结束后同步数据窗口) */
    private Integer bufferDays;

    /** 报名开始时间 */
    private Date registerStart;

    /** 报名截止时间 */
    private Date registerEnd;

    /** 状态(0草稿 1进行中 2已结束) */
    private Integer status;

    /** 主办单位(关联sys_dept) */
    private Long orgId;

    @TableLogic
    private Long delFlag;
}
