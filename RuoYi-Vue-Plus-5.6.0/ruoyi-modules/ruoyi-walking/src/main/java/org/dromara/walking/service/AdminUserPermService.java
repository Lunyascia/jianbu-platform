package org.dromara.walking.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dromara.common.mybatis.domain.WalkingUserMenu;
import org.dromara.common.mybatis.mapper.WalkingUserMenuMapper;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.walking.domain.vo.WalkingMenuNode;
import org.dromara.walking.mapper.WalkingUserPermMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员每用户行走菜单权限配置服务
 */
@Service
public class AdminUserPermService {

    @Autowired
    private WalkingUserMenuMapper walkingUserMenuMapper;
    @Autowired
    private WalkingUserPermMapper walkingUserPermMapper;

    /** 该管理员自定义的行走菜单id列表 */
    public List<Long> getUserMenuIds(Long userId) {
        return walkingUserMenuMapper.selectMenuIdsByUserId(userId);
    }

    /** 覆盖保存单个管理员的行走菜单权限 */
    @Transactional(rollbackFor = Exception.class)
    public void saveUserMenu(Long userId, List<Long> menuIds) {
        walkingUserMenuMapper.delete(
            new LambdaQueryWrapper<WalkingUserMenu>().eq(WalkingUserMenu::getUserId, userId));
        if (CollUtil.isEmpty(menuIds)) {
            WalkingUserMenu marker = new WalkingUserMenu();
            marker.setUserId(userId);
            marker.setMenuId(WalkingUserMenuMapper.EMPTY_OVERRIDE_MENU_ID);
            marker.setCreateBy(LoginHelper.getUserId());
            marker.setCreateTime(new Date());
            walkingUserMenuMapper.insert(marker);
        } else {
            for (Long menuId : menuIds) {
                WalkingUserMenu um = new WalkingUserMenu();
                um.setUserId(userId);
                um.setMenuId(menuId);
                um.setCreateBy(LoginHelper.getUserId());
                um.setCreateTime(new Date());
                walkingUserMenuMapper.insert(um);
            }
        }
    }

    /** 批量给多个管理员套用同一组行走菜单权限 */
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveUserMenu(List<Long> userIds, List<Long> menuIds) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        for (Long uid : userIds) {
            saveUserMenu(uid, menuIds);
        }
    }

    /** 行走菜单权限树 + 已勾选菜单id（权限配置弹窗用） */
    public Map<String, Object> getUserPermTree(Long userId) {
        List<WalkingMenuNode> flat = walkingUserPermMapper.selectWalkingMenuFlat();
        Map<String, Object> result = new HashMap<>();
        result.put("menus", buildTree(flat));
        result.put("checkedKeys", getUserMenuIds(userId));
        return result;
    }

    private List<WalkingMenuNode> buildTree(List<WalkingMenuNode> flat) {
        Map<Long, WalkingMenuNode> map = new HashMap<>();
        flat.forEach(n -> map.put(n.getMenuId(), n));
        List<WalkingMenuNode> roots = new ArrayList<>();
        for (WalkingMenuNode n : flat) {
            WalkingMenuNode parent = map.get(n.getParentId());
            if (parent != null) {
                parent.getChildren().add(n);
            } else {
                roots.add(n);
            }
        }
        return roots;
    }
}
