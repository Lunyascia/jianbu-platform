package org.dromara.walking.domain.vo.admin;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 单位统计（后台）—— 报名/参与/打卡/获奖
 */
@Data
@ExcelIgnoreUnannotated
public class DeptStatVo {

    /** 单位id */
    private Long deptId;

    /** 单位名称 */
    @ExcelProperty(value = "单位")
    private String deptName;

    /** 会员总数（线下统计手动维护） */
    @ExcelProperty(value = "会员总数")
    private Long memberTotal;

    /** 报名人数(status 1/2) */
    @ExcelProperty(value = "报名人数")
    private Long regCount;

    /** 参与人数(status 2通过) */
    @ExcelProperty(value = "参与人数")
    private Long approvedCount;

    /** 参与率(0-1) */
    @ExcelProperty(value = "参与率")
    private String participationRateText;

    /** 打卡达标人数 */
    @ExcelProperty(value = "打卡达标人数")
    private Long checkinCount;

    /** 打卡率(达标人数/参与人数, 0-1) */
    @ExcelProperty(value = "打卡率")
    private String checkinRateText;

    /** 获奖人数 */
    @ExcelProperty(value = "获奖人数")
    private Long awardCount;

    /** 累计步数 */
    @ExcelProperty(value = "累计步数")
    private Long totalSteps;

    /** 累计积分 */
    @ExcelProperty(value = "累计积分")
    private Long totalPoints;

    // ---- 内部计算字段 ----
    private Double participationRate;
    private Double checkinRate;
    private Long award1Count;
    private Long award2Count;
    private Long award3Count;
    private Long awardExcellentCount;
}
