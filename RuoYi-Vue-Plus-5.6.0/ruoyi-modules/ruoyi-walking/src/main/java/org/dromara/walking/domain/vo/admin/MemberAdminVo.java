package org.dromara.walking.domain.vo.admin;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 会员管理（后台）视图对象
 */
@Data
@ExcelIgnoreUnannotated
public class MemberAdminVo {

    /** 会员id */
    private Long id;

    /** 微信openid */
    private String openid;

    /** 是否已登录小程序(openid非空) 0否 1是 */
    private Integer loggedIn;

    /** 手机号 */
    @ExcelProperty(value = "手机号")
    private String phone;

    /** 姓名 */
    @ExcelProperty(value = "姓名")
    private String realName;

    /** 身份证号 */
    private String idCard;

    /** 单位id */
    private Long deptId;

    /** 单位名称 */
    @ExcelProperty(value = "单位")
    private String deptName;

    /** 状态(0正常 1停用) */
    private Integer status;

    /** 是否审核锁定(0否 1是) */
    private Integer isVerified;

    /** 注册/首次登录时间 */
    @ExcelProperty(value = "注册时间", format = "yyyy-MM-dd HH:mm:ss")
    private Date registerTime;

    /** 收货人 */
    private String receiver;

    /** 收货手机号 */
    private String addressPhone;

    /** 收货地址 */
    private String address;

    /** 累计步数(关联统计用) */
    private Long totalSteps;

    /** 累计积分 */
    private Long totalPoints;

    /** 报名数 */
    private Long regCount;
}
