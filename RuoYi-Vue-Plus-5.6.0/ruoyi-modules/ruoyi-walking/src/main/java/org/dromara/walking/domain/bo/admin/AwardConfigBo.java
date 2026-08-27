package org.dromara.walking.domain.bo.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 奖励档位管理（后台）表单
 */
@Data
public class AwardConfigBo {

    /** 奖励id(新增为空) */
    private Long id;

    /** 活动id */
    @NotNull(message = "所属活动不能为空")
    private Long activityId;

    /** 类型(1个人 2集体) */
    @NotNull(message = "奖励类型不能为空")
    private Integer awardType;

    /** 奖励名称 */
    @NotBlank(message = "奖励名称不能为空")
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
