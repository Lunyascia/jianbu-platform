package org.dromara.walking.domain.vo;

import lombok.Data;

/**
 * 打卡日历记录
 */
@Data
public class StepRecordVo {
    /** 日期 yyyy-MM-dd */
    private String date;
    /** 步数 */
    private Integer steps;
    /** 是否达标(≥7000) */
    private Boolean reached;
    /** 是否有打卡记录 */
    private Boolean hasRecord;
}
