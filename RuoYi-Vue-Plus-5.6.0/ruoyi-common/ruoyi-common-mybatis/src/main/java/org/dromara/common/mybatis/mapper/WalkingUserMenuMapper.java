package org.dromara.common.mybatis.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.common.mybatis.domain.WalkingUserMenu;

import java.util.List;

/**
 * 管理员每用户行走菜单权限 Mapper
 */
@Mapper
public interface WalkingUserMenuMapper extends BaseMapperPlus<WalkingUserMenu, WalkingUserMenu> {

    /** menu_id=0 表示用户已配置过权限，但明确不授予任何行走菜单。 */
    long EMPTY_OVERRIDE_MENU_ID = 0L;

    /** 查询用户自定义的行走菜单id列表（无则返回空） */
    default List<Long> selectMenuIdsByUserId(Long userId) {
        return selectList(new LambdaQueryWrapper<WalkingUserMenu>()
                .eq(WalkingUserMenu::getUserId, userId)
                .ne(WalkingUserMenu::getMenuId, EMPTY_OVERRIDE_MENU_ID))
            .stream().map(WalkingUserMenu::getMenuId).toList();
    }

    /** 是否存在用户级覆盖配置，包括明确配置为空的情况。 */
    default boolean hasUserOverride(Long userId) {
        return exists(new LambdaQueryWrapper<WalkingUserMenu>()
            .eq(WalkingUserMenu::getUserId, userId));
    }
}
