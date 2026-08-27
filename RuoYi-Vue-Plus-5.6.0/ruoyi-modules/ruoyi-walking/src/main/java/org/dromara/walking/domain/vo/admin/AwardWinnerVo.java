package org.dromara.walking.domain.vo.admin;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 中奖名单（后台）视图对象 —— 证书 + 会员信息
 */
@Data
@ExcelIgnoreUnannotated
public class AwardWinnerVo {

    /** 证书id */
    private Long id;

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

    /** 奖项级别 */
    @ExcelProperty(value = "奖项")
    private String awardLevel;

    /** 名次 */
    @ExcelProperty(value = "名次")
    private Integer rank;

    /** 收货人 */
    @ExcelProperty(value = "收货人")
    private String receiver;

    /** 收货手机号 */
    @ExcelProperty(value = "收货手机号")
    private String addressPhone;

    /** 收货地址 */
    @ExcelProperty(value = "收货地址")
    private String address;

    /** 会员状态(0正常 1停用) */
    private Integer memberStatus;

    /** 颁发时间 */
    @ExcelProperty(value = "颁发时间", format = "yyyy-MM-dd HH:mm:ss")
    private Date issueTime;
}
