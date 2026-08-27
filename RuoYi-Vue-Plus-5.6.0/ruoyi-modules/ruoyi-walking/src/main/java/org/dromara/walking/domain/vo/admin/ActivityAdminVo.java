package org.dromara.walking.domain.vo.admin;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import lombok.Data;

import java.util.Date;

/**
 * 活动管理（后台）视图对象
 */
@Data
@ExcelIgnoreUnannotated
public class ActivityAdminVo {

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

    /** 状态文案 */
    private String statusText;

    /** 主办单位 */
    private Long orgId;

    /** 报名人数(状态1/2) */
    private Long regCount;
}
