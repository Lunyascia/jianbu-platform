package org.dromara.walking.domain.vo;

import lombok.Data;

/**
 * 今日步数统计
 */
@Data
public class TodayStepVo {
    /** 今日是否已同步打卡 */
    private Boolean syncedToday;
    /** 今日步数 */
    private Integer todaySteps;
    /** 是否达标(≥7000) */
    private Boolean reached;
    /** 累计达标打卡天数 */
    private Integer checkInDays;
    /** 当前连续打卡天数 */
    private Integer streakDays;
    /** 总积分 */
    private Integer totalPoints;
    /** 活动总步数 */
    private Integer totalSteps;
}
