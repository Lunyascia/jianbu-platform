package org.dromara.common.mybatis.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 管理员每用户行走菜单权限
 * 放在 common-mybatis 供 ruoyi-system(菜单/权限加载 hook) 与 ruoyi-walking(配置接口) 共用
 */
@Data
@TableName("walking_user_menu")
public class WalkingUserMenu {

    @TableId(value = "id")
    private Long id;

    /** 管理员用户id */
    private Long userId;

    /** 允许访问的行走菜单id(4000目录下) */
    private Long menuId;

    private Long createBy;

    private Date createTime;
}
