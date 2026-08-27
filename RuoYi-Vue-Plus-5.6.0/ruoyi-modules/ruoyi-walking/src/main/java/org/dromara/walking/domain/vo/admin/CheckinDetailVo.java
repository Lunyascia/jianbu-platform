package org.dromara.walking.domain.vo.admin;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 个人打卡明细（后台）—— 每日步数/达标/同步方式
 */
@Data
@ExcelIgnoreUnannotated
public class CheckinDetailVo {

    private Long memberId;

    private String realName;

    private String phone;

    private String deptName;

    /** 打卡日期 */
    @ExcelProperty(value = "日期")
    private String recordDate;

    /** 步数 */
    @ExcelProperty(value = "步数")
    private Integer steps;

    /** 当日目标 */
    private Integer dailyTarget;

    /** 是否达标 */
    @ExcelProperty(value = "达标")
    private String reachedText;

    /** 来源(1微信运动 2人工) */
    private Integer source;

    /** 同步方式 */
    @ExcelProperty(value = "同步方式")
    private String sourceText;

    /** 异常标志 */
    private Integer abnormalFlag;

    /** 是否锁定 */
    private Integer locked;
}
