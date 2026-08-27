package org.dromara.walking.domain.vo.admin;

import lombok.Data;

/**
 * 数据统计总览（后台）
 */
@Data
public class StatsOverviewVo {

    /** 活动总数 */
    private Long totalActivities;

    /** 进行中活动数 */
    private Long activeActivities;

    /** 会员总数（小程序注册） */
    private Long totalMembers;

    /** 组织会员总数（线下统计手动维护，参与率分母） */
    private Long orgMemberTotal;

    /** 报名总数(草稿+待审核+通过) */
    private Long totalRegistrations;

    /** 审核通过报名数 */
    private Long approvedCount;

    /** 已取消数 */
    private Long cancelledCount;

    /** 已停用数 */
    private Long disabledCount;

    /** 参与率(通过/会员) */
    private String participationRate;

    /** 单位数 */
    private Long deptCount;

    /** 总步数 */
    private Long totalSteps;

    /** 异常数据条数 */
    private Long abnormalCount;
}
