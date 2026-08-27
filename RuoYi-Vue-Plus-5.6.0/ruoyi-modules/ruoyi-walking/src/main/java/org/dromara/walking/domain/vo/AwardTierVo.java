package org.dromara.walking.domain.vo;

import lombok.Data;

/**
 * 奖励档位（公开，H5 报名页 / 小程序奖励页展示）
 */
@Data
public class AwardTierVo {

    /** 奖励id */
    private Long id;

    /** 活动id */
    private Long activityId;

    /** 类型(1个人 2集体) */
    private Integer awardType;

    /** 奖励名称 */
    private String awardName;

    /** 名次/名额区间起 */
    private Integer rankStart;

    /** 名次/名额区间止(0表示"若干") */
    private Integer rankEnd;

    /** 奖励内容 */
    private String prizeContent;

    /** 奖品图片 */
    private String imageUrl;

    /** 状态(0停用 1启用) */
    private Integer status;

    /** 排序(越小越靠前) */
    private Integer sortOrder;
}
