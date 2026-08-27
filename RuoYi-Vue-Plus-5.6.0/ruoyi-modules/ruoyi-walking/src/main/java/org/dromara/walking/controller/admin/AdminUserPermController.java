package org.dromara.walking.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.walking.domain.bo.admin.UserPermBatchBo;
import org.dromara.walking.domain.bo.admin.UserPermBo;
import org.dromara.walking.service.AdminUserPermService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理员每用户行走菜单权限配置（后台）
 */
@Validated
@RestController
@RequestMapping("/walking/admin/user-perm")
public class AdminUserPermController {

    @Autowired
    private AdminUserPermService permService;

    /** 行走菜单权限树 + 已勾选 */
    @SaCheckPermission("walking:userPerm:edit")
    @GetMapping("/{userId}")
    public R<Map<String, Object>> tree(@PathVariable Long userId) {
        return R.ok(permService.getUserPermTree(userId));
    }

    /** 保存单个管理员的行走菜单权限 */
    @SaCheckPermission("walking:userPerm:edit")
    @Log(title = "健步走管理员权限配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/save")
    public R<Void> save(@Validated @RequestBody UserPermBo bo) {
        permService.saveUserMenu(bo.getUserId(), bo.getMenuIds());
        return R.ok();
    }

    /** 批量给多个管理员配置同一组行走菜单权限 */
    @SaCheckPermission("walking:userPerm:edit")
    @Log(title = "健步走管理员权限批量配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/batch")
    public R<Void> batch(@Validated @RequestBody UserPermBatchBo bo) {
        permService.batchSaveUserMenu(bo.getUserIds(), bo.getMenuIds());
        return R.ok();
    }
}
