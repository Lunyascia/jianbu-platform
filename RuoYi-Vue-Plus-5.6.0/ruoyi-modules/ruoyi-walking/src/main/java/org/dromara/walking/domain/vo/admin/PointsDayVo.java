package org.dromara.walking.domain.vo.admin;

import lombok.Data;

/**
 * 积分逐日明细行
 */
@Data
public class PointsDayVo {

    /** 打卡日期 */
    private String recordDate;

    /** 步数 */
    private Integer steps;

    /** 是否达标 */
    private Boolean reached;

    /** 当日基础分 */
    private Integer basePoints;

    /** 当日阶段奖励分 */
    private Integer bonusPoints;

    /** 当日小计 */
    private Integer dayTotal;

    /** 累计积分 */
    private Integer cumulative;
}
