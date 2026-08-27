package org.dromara.walking.domain.bo.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 管理员用户行走菜单权限保存表单
 */
@Data
public class UserPermBo {

    /** 管理员用户id */
    @NotNull(message = "用户id不能为空")
    private Long userId;

    /** 允许的行走菜单id列表 */
    private List<Long> menuIds;
}
