package org.dromara.walking.domain.bo.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 中奖名单标记（后台）表单
 */
@Data
public class AwardBo {

    /** 活动id */
    @NotNull(message = "活动id不能为空")
    private Long activityId;

    /** 会员id */
    @NotNull(message = "会员id不能为空")
    private Long memberId;

    /** 奖项级别(一等奖/二等奖/三等奖/优秀奖) */
    @NotBlank(message = "奖项级别不能为空")
    private String awardLevel;

    /** 名次(可选) */
    private Integer rank;
}
