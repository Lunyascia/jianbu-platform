package org.dromara.walking.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 行走模块菜单节点（权限配置树用）
 */
@Data
public class WalkingMenuNode {

    private Long menuId;
    private Long parentId;
    private String menuName;
    /** M=目录 C=菜单 F=按钮 */
    private String menuType;
    private String perms;
    private Integer orderNum;
    private List<WalkingMenuNode> children = new ArrayList<>();
}
