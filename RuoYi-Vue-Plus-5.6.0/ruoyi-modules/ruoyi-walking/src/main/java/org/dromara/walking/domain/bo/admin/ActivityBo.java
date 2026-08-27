package org.dromara.walking.domain.bo.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

/**
 * 活动管理（后台）表单
 */
@Data
public class ActivityBo {

    /** 活动id(新增为空) */
    private Long id;

    /** 活动名称 */
    @NotBlank(message = "活动名称不能为空")
    private String activityName;

    /** 封面图 */
    private String coverUrl;

    /** 活动介绍 */
    private String description;

    /** 活动规则 */
    private String ruleContent;

    /** 开始日期 */
    @NotNull(message = "活动开始日期不能为空")
    private Date startDate;

    /** 结束日期 */
    @NotNull(message = "活动结束日期不能为空")
    private Date endDate;

    /** 目标总步数 */
    private Integer targetSteps;

    /** 每日目标步数 */
    private Integer dailyTargetSteps;

    /** 每千步积分 */
    private Integer pointsPerThousandSteps;

    /** 单日步数上限 */
    private Integer dailyStepLimit;

    /** 连续7天奖励分 */
    private Integer streak7Points;

    /** 连续14天奖励分 */
    private Integer streak14Points;

    /** 全程全勤奖励分 */
    private Integer fullAttendancePoints;

    /** 缓冲期时长(天) */
    private Integer bufferDays;

    /** 报名开始时间 */
    private Date registerStart;

    /** 报名截止时间 */
    private Date registerEnd;

    /** 状态(0草稿 1进行中 2已结束) */
    private Integer status;

    /** 主办单位 */
    private Long orgId;
}
