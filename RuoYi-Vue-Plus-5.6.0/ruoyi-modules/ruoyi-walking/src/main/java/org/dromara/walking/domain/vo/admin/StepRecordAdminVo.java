package org.dromara.walking.domain.vo.admin;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.sql.Date;

/**
 * 打卡信息（后台导出用）
 */
@Data
@ExcelIgnoreUnannotated
public class StepRecordAdminVo {

    /** 会员id */
    private Long memberId;

    /** 活动id */
    private Long activityId;

    /** 姓名 */
    @ExcelProperty(value = "姓名")
    private String realName;

    /** 手机号 */
    @ExcelProperty(value = "手机号")
    private String phone;

    /** 单位 */
    @ExcelProperty(value = "单位")
    private String deptName;

    /** 打卡日期 */
    @ExcelProperty(value = "打卡日期")
    private Date recordDate;

    /** 步数 */
    @ExcelProperty(value = "步数")
    private Integer steps;

    /** 来源(1微信运动 2人工) */
    private Integer source;

    /** 异常标志(0正常 1异常) */
    private Integer abnormalFlag;
}
