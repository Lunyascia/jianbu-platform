package org.dromara.walking.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.dromara.walking.domain.vo.WalkingMenuNode;

import java.util.List;

/**
 * 行走模块菜单查询（直接查 sys_menu，权限配置树用）
 */
@Mapper
public interface WalkingUserPermMapper {

    /** 行走模块菜单扁平列表，包含迁移到系统目录下的 walking 权限菜单。 */
    @Select("SELECT menu_id AS menuId, parent_id AS parentId, menu_name AS menuName, " +
        "menu_type AS menuType, perms, order_num AS orderNum " +
        "FROM sys_menu WHERE status = '0' " +
        "AND (menu_id = 4000 OR perms LIKE 'walking:%') " +
        "ORDER BY parent_id, order_num")
    List<WalkingMenuNode> selectWalkingMenuFlat();
}
