-- =============================================================
-- 健步走线上活动系统 组织机构管理 扩展脚本
-- 目标库: ry-vue
-- 内容:
--   1. sys_dept 增加"会员总数"列(walking_member_total)，线下统计、管理员手动维护
--   2. 示例组织机构树(总工会 → 镇工会 → 单位工会)，dept_category='union' 标识工会组织
--   3. 健步走"组织机构管理/H5报名单位下拉"仅显示 dept_category='union' 的组织
-- 注意: ALTER 仅需执行一次，重复执行会报"Duplicate column"
-- 执行: mysql --default-character-set=utf8mb4 -uroot -p123456 ry-vue < walking_org.sql
-- =============================================================

-- -------------------------------------------------------------
-- 1. sys_dept 增加会员总数列
-- -------------------------------------------------------------
ALTER TABLE `sys_dept`
  ADD COLUMN `walking_member_total` int NOT NULL DEFAULT 0
  COMMENT '会员总数(线下统计,管理员手动维护)' AFTER `phone`;

-- -------------------------------------------------------------
-- 2. 示例组织机构树（示意数据，请按真实工会层级调整/删除后使用）
--    结构: 总工会 → 镇工会 → 单位工会
-- -------------------------------------------------------------
-- 一级：县总工会
INSERT INTO sys_dept(dept_id, tenant_id, parent_id, ancestors, dept_name, dept_category, order_num, status, del_flag, create_time)
VALUES(7000, '000000', 0, '0', '藤县总工会', 'union', 1, '0', '0', sysdate());

-- 二级：镇工会
INSERT INTO sys_dept(dept_id, tenant_id, parent_id, ancestors, dept_name, dept_category, order_num, status, del_flag, create_time)
VALUES(7100, '000000', 7000, '0,7000', '太平镇工会', 'union', 1, '0', '0', sysdate());
INSERT INTO sys_dept(dept_id, tenant_id, parent_id, ancestors, dept_name, dept_category, order_num, status, del_flag, create_time)
VALUES(7101, '000000', 7000, '0,7000', '埌南镇工会', 'union', 2, '0', '0', sysdate());
INSERT INTO sys_dept(dept_id, tenant_id, parent_id, ancestors, dept_name, dept_category, order_num, status, del_flag, create_time)
VALUES(7102, '000000', 7000, '0,7000', '藤州镇工会', 'union', 3, '0', '0', sysdate());

-- 三级：单位工会（挂在不同镇工会下）
INSERT INTO sys_dept(dept_id, tenant_id, parent_id, ancestors, dept_name, dept_category, order_num, status, del_flag, create_time, walking_member_total)
VALUES(7200, '000000', 7100, '0,7000,7100', '太平镇第一小学工会', 'union', 1, '0', '0', sysdate(), 120);
INSERT INTO sys_dept(dept_id, tenant_id, parent_id, ancestors, dept_name, dept_category, order_num, status, del_flag, create_time, walking_member_total)
VALUES(7201, '000000', 7100, '0,7000,7100', '太平镇中心卫生院工会', 'union', 2, '0', '0', sysdate(), 85);
INSERT INTO sys_dept(dept_id, tenant_id, parent_id, ancestors, dept_name, dept_category, order_num, status, del_flag, create_time, walking_member_total)
VALUES(7202, '000000', 7101, '0,7000,7101', '埌南镇人民政府工会', 'union', 1, '0', '0', sysdate(), 60);
INSERT INTO sys_dept(dept_id, tenant_id, parent_id, ancestors, dept_name, dept_category, order_num, status, del_flag, create_time, walking_member_total)
VALUES(7203, '000000', 7102, '0,7000,7102', '藤州镇中学工会', 'union', 1, '0', '0', sysdate(), 200);
