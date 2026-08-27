package org.dromara.walking.domain.bo.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 异常数据/作弊账号批量处理表单（后台）
 */
@Data
public class CheatBatchBo {

    /** 会员id列表 */
    private List<Long> memberIds;

    /** 步数记录id列表 */
    private List<Long> recordIds;

    /** 活动id */
    @NotNull(message = "活动id不能为空")
    private Long activityId;

    /**
     * 处理方式:
     * 1 标记异常
     * 2 删除异常数据
     * 3 停用账号
     * 4 取消报名
     * 5 恢复异常（取消标记）
     */
    @NotNull(message = "处理方式不能为空")
    private Integer handleType;

    /** 备注 */
    private String remark;
}
