-- =============================================================
-- 健步走线上活动系统 活动参数配置 + 中奖名单 扩展脚本
-- 目标库: ry-vue
-- 内容:
--   1. walking_activity 增加可配置参数列（单日步数上限/连击奖励/全勤奖励/缓冲期）
--   2. walking_certificate 增加奖项级别列(award_level)
--   3. 新增后台菜单 4008 中奖名单管理（含按钮权限 + 角色映射）
-- 注意: ALTER 仅需执行一次，重复执行会报"Duplicate column"
-- 执行: mysql --default-character-set=utf8mb4 -uroot -p123456 ry-vue < walking_config_award.sql
-- =============================================================

-- -------------------------------------------------------------
-- 1. 活动表新增可配置参数
-- -------------------------------------------------------------
ALTER TABLE `walking_activity`
  ADD COLUMN `daily_step_limit` int NOT NULL DEFAULT 15000 COMMENT '单日步数上限(超出不计入统计)' AFTER `points_per_thousand_steps`,
  ADD COLUMN `streak7_points` int NOT NULL DEFAULT 2 COMMENT '连续7天奖励分' AFTER `daily_step_limit`,
  ADD COLUMN `streak14_points` int NOT NULL DEFAULT 5 COMMENT '连续14天奖励分' AFTER `streak7_points`,
  ADD COLUMN `full_attendance_points` int NOT NULL DEFAULT 10 COMMENT '全程全勤奖励分' AFTER `streak14_points`,
  ADD COLUMN `buffer_days` int NOT NULL DEFAULT 1 COMMENT '缓冲期时长(天,活动结束后同步数据窗口)' AFTER `full_attendance_points`;

-- -------------------------------------------------------------
-- 2. 证书表增加奖项级别
-- -------------------------------------------------------------
ALTER TABLE `walking_certificate`
  ADD COLUMN `award_level` varchar(20) DEFAULT '' COMMENT '奖项级别(一等奖/二等奖/三等奖/优秀奖)' AFTER `rank`;

-- -------------------------------------------------------------
-- 3. 后台菜单：中奖名单管理（4008）
-- -------------------------------------------------------------
INSERT INTO sys_menu VALUES(4008, '中奖名单管理', 4000, 8, 'award', 'walking/award/index', '', 1, 0, 'C', '0', '0', 'walking:award:list', 'trophy', 103, 1, sysdate(), NULL, NULL, '中奖名单管理菜单');
INSERT INTO sys_menu VALUES(40081, '名单查询', 4008, 1, '', '', '', 1, 0, 'F', '0', '0', 'walking:award:query', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40082, '标记中奖', 4008, 2, '', '', '', 1, 0, 'F', '0', '0', 'walking:award:mark', '#', 103, 1, sysdate(), NULL, NULL, '管理员标记/调整中奖名单');
INSERT INTO sys_menu VALUES(40083, '删除名单', 4008, 3, '', '', '', 1, 0, 'F', '0', '0', 'walking:award:remove', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40084, '导出名单', 4008, 4, '', '', '', 1, 0, 'F', '0', '0', 'walking:award:export', '#', 103, 1, sysdate(), NULL, NULL, '导出中奖用户信息(姓名/手机号/收货地址)');

-- 系统管理员(100)：全部
INSERT INTO sys_role_menu VALUES(100, 4008);
INSERT INTO sys_role_menu VALUES(100, 40081);
INSERT INTO sys_role_menu VALUES(100, 40082);
INSERT INTO sys_role_menu VALUES(100, 40083);
INSERT INTO sys_role_menu VALUES(100, 40084);
-- 管理员(101)：名单查看/导出，标记权限默认不给(可勾选)
INSERT INTO sys_role_menu VALUES(101, 4008);
INSERT INTO sys_role_menu VALUES(101, 40081);
INSERT INTO sys_role_menu VALUES(101, 40084);
