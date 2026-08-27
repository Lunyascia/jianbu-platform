package org.dromara.walking.domain.vo.admin;

import lombok.Data;

import java.util.Date;

/**
 * 作弊处理审计（后台）
 */
@Data
public class CheatLogVo {

    private Long id;

    /** 会员id */
    private Long memberId;

    /** 会员姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 活动id */
    private Long activityId;

    /** 异常数据日期 */
    private Date recordDate;

    /** 异常类型(步数超限/数据突增/非活动期) */
    private String abnormalType;

    /** 处理方式(1标记 2删数据 3停用 4取消) */
    private Integer handleType;

    /** 处理方式文案 */
    private String handleTypeText;

    /** 处理管理员 */
    private String operator;

    /** 备注 */
    private String remark;

    /** 处理时间 */
    private Date createTime;
}
