package org.dromara.walking.domain.bo.admin;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 管理员用户行走菜单权限批量配置表单
 */
@Data
public class UserPermBatchBo {

    /** 管理员用户id列表 */
    @NotEmpty(message = "请选择管理员")
    private List<Long> userIds;

    /** 允许的行走菜单id列表 */
    private List<Long> menuIds;
}
