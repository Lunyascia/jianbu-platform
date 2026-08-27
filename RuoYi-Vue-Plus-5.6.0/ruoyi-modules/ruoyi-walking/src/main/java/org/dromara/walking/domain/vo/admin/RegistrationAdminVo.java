package org.dromara.walking.domain.vo.admin;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 报名管理（后台）视图对象 —— 报名记录 + 会员信息
 */
@Data
@ExcelIgnoreUnannotated
public class RegistrationAdminVo {

    /** 报名id */
    private Long id;

    /** 会员id */
    private Long memberId;

    /** 活动id */
    private Long activityId;

    /** 活动名称 */
    private String activityName;

    /** 姓名 */
    @ExcelProperty(value = "姓名")
    private String realName;

    /** 手机号 */
    @ExcelProperty(value = "手机号")
    private String phone;

    /** 单位id */
    private Long deptId;

    /** 单位名称 */
    @ExcelProperty(value = "单位")
    private String deptName;

    /** 身份证号 */
    @ExcelProperty(value = "身份证号")
    private String idCard;

    /** 会员状态(0正常 1停用) */
    private Integer memberStatus;

    /** 报名状态(0草稿 1待审核 2通过 3取消 4停用) */
    private Integer status;

    /** 状态文案 */
    private String statusText;

    /** 审核结果/异常原因 */
    @ExcelProperty(value = "审核结果")
    private String auditResult;

    /** 提交时间 */
    @ExcelProperty(value = "提交时间", format = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    /** 审核时间 */
    private Date auditTime;

    /** 取消/撤下操作人 */
    private String cancelBy;

    /** 取消/撤下时间 */
    private Date cancelTime;
}
