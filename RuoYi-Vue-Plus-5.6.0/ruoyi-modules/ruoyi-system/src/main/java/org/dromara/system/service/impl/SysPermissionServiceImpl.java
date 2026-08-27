package org.dromara.system.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.TenantConstants;
import org.dromara.common.core.service.PermissionService;
import org.dromara.common.mybatis.mapper.WalkingUserMenuMapper;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.service.ISysMenuService;
import org.dromara.system.service.ISysPermissionService;
import org.dromara.system.service.ISysRoleService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户权限处理
 *
 * @author ruoyi
 */
@RequiredArgsConstructor
@Service
public class SysPermissionServiceImpl implements ISysPermissionService, PermissionService {

    private final ISysRoleService roleService;
    private final ISysMenuService menuService;
    private final WalkingUserMenuMapper walkingUserMenuMapper;

    /**
     * 获取角色数据权限
     *
     * @param userId  用户id
     * @return 角色权限信息
     */
    @Override
    public Set<String> getRolePermission(Long userId) {
        Set<String> roles = new HashSet<>();
        // 管理员拥有所有权限
        if (LoginHelper.isSuperAdmin(userId)) {
            roles.add(TenantConstants.SUPER_ADMIN_ROLE_KEY);
        } else {
            roles.addAll(roleService.selectRolePermissionByUserId(userId));
        }
        return roles;
    }

    /**
     * 获取菜单数据权限
     *
     * @param userId  用户id
     * @return 菜单权限信息
     */
    @Override
    public Set<String> getMenuPermission(Long userId) {
        Set<String> perms = new HashSet<>();
        // 管理员拥有所有权限
        if (LoginHelper.isSuperAdmin(userId)) {
            perms.add("*:*:*");
        } else {
            perms.addAll(menuService.selectMenuPermsByUserId(userId));
            // 每用户自定义行走菜单：有 walking_user_menu 记录的管理员，行走权限以自定义为准
            if (walkingUserMenuMapper.hasUserOverride(userId)) {
                List<Long> customIds = walkingUserMenuMapper.selectMenuIdsByUserId(userId);
                perms.removeIf(p -> p.startsWith("walking:"));
                if (!customIds.isEmpty()) {
                    perms.addAll(menuService.selectMenuPermsByMenuIds(customIds));
                }
            }
        }
        return perms;
    }
}
