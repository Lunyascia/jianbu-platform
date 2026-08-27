-- =============================================================
-- 健步走后台权限缺陷修复（可在已有数据库重复执行）
-- 1. 补齐报名审核、管理员权限配置两个实际使用的权限
-- 2. 补齐 walking_user_menu 表，支持用户级权限覆盖
-- =============================================================

CREATE TABLE IF NOT EXISTS `walking_user_menu` (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '后台管理员用户id',
  `menu_id` bigint NOT NULL COMMENT '允许访问的菜单id，0表示明确清空',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_walking_user_menu` (`user_id`,`menu_id`),
  KEY `idx_walking_user_menu_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='后台管理员健步走菜单权限覆盖表';

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame,
   is_cache, menu_type, visible, status, perms, icon, create_dept, create_by,
   create_time, update_by, update_time, remark)
SELECT
  40026, '报名审核', 4002, 6, '', '', '', 1,
  0, 'F', '0', '0', 'walking:registration:approve', '#', 103, 1,
  sysdate(), NULL, NULL, '审核报名通过或驳回'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 40026);

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame,
   is_cache, menu_type, visible, status, perms, icon, create_dept, create_by,
   create_time, update_by, update_time, remark)
SELECT
  40001, '管理员权限配置', 4000, 8, '', '', '', 1,
  0, 'F', '0', '0', 'walking:userPerm:edit', '#', 103, 1,
  sysdate(), NULL, NULL, '系统管理员专属: 配置管理员的健步走菜单与按钮权限'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 40001);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 100, 40026
WHERE NOT EXISTS (
  SELECT 1 FROM sys_role_menu WHERE role_id = 100 AND menu_id = 40026
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 100, 40001
WHERE NOT EXISTS (
  SELECT 1 FROM sys_role_menu WHERE role_id = 100 AND menu_id = 40001
);
