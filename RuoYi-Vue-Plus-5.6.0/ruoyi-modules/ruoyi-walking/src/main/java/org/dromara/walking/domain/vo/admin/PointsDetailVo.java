package org.dromara.walking.domain.vo.admin;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 个人积分明细（后台）—— 基础分 + 阶段奖励
 * 积分规则（与小程序端一致）：
 *  基础分：每日达标(≥目标步数)+1
 *  阶段奖励：连续7天+2(最多3次)、连续14天+5(最多2次)、21天全勤+10
 */
@Data
public class PointsDetailVo {

    private Long memberId;

    private String realName;

    private String phone;

    private String deptName;

    /** 活动id */
    private Long activityId;

    /** 基础分 */
    private Integer basePoints;

    /** 阶段奖励分 */
    private Integer bonusPoints;

    /** 总积分 */
    private Integer totalPoints;

    /** 逐日明细 */
    private List<PointsDayVo> daily = new ArrayList<>();
}
