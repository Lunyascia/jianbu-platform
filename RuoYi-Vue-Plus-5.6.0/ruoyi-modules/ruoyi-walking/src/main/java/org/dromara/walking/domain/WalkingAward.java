package org.dromara.walking.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 健步走奖励档位
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("walking_award")
public class WalkingAward extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 活动id */
    private Long activityId;

    /** 类型(1个人 2集体) */
    private Integer awardType;

    /** 奖励名称 */
    private String awardName;

    /** 名次/名额区间起(集体奖表示名额起) */
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

    @TableLogic
    private Long delFlag;
}
