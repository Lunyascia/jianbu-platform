-- =============================================================
-- 健步走 奖励档位管理 扩展脚本
-- 目标库: ry-vue
-- 内容:
--   1. walking_award 增加 award_type(个人/集体)、sort_order(排序) 列
--   2. 后台新增菜单 4009 奖励管理（含按钮权限 + 角色映射）
--   3. 为活动 1001 初始化默认奖励档位（个人 4 档 + 集体 1 档）
-- 说明:
--   - 修改奖励后，H5 报名页 / 小程序奖励页通过 /walking/award/list 自动展示最新结果
--   - ALTER 仅需执行一次，重复执行会报 "Duplicate column"；数据部分请勿重复执行
-- 执行: mysql --default-character-set=utf8mb4 -uroot -p123456 ry-vue < walking_award_config.sql
-- =============================================================

-- -------------------------------------------------------------
-- 1. walking_award 表结构扩展
-- -------------------------------------------------------------
ALTER TABLE `walking_award`
  ADD COLUMN `award_type` int NOT NULL DEFAULT 1 COMMENT '类型(1个人 2集体)' AFTER `activity_id`,
  ADD COLUMN `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序(越小越靠前)' AFTER `status`;

-- -------------------------------------------------------------
-- 2. 后台菜单：奖励管理（4009，挂健步走管理 4000 下，顺序第 9）
-- -------------------------------------------------------------
INSERT INTO sys_menu VALUES(4009, '奖励管理', 4000, 9, 'awardConfig', 'walking/awardConfig/index', '', 1, 0, 'C', '0', '0', 'walking:awardConfig:list', 'present', 103, 1, sysdate(), NULL, NULL, '奖励档位管理菜单(修改后H5/小程序即时生效)');
INSERT INTO sys_menu VALUES(40091, '奖励查询', 4009, 1, '', '', '', 1, 0, 'F', '0', '0', 'walking:awardConfig:query', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40092, '奖励新增', 4009, 2, '', '', '', 1, 0, 'F', '0', '0', 'walking:awardConfig:add', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40093, '奖励修改', 4009, 3, '', '', '', 1, 0, 'F', '0', '0', 'walking:awardConfig:edit', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40094, '奖励删除', 4009, 4, '', '', '', 1, 0, 'F', '0', '0', 'walking:awardConfig:remove', '#', 103, 1, sysdate(), NULL, NULL, '');

-- 系统管理员(100)：全部
INSERT INTO sys_role_menu VALUES(100, 4009);
INSERT INTO sys_role_menu VALUES(100, 40091);
INSERT INTO sys_role_menu VALUES(100, 40092);
INSERT INTO sys_role_menu VALUES(100, 40093);
INSERT INTO sys_role_menu VALUES(100, 40094);
-- 管理员(101)：查询/新增/修改，删除默认不给
INSERT INTO sys_role_menu VALUES(101, 4009);
INSERT INTO sys_role_menu VALUES(101, 40091);
INSERT INTO sys_role_menu VALUES(101, 40092);
INSERT INTO sys_role_menu VALUES(101, 40093);

-- -------------------------------------------------------------
-- 3. 活动 1001 默认奖励档位（如已用后台新增过，请勿重复执行本段）
--    规则约定: rank_end=0 表示"若干名/若干家"；集体奖 award_type=2
-- -------------------------------------------------------------
INSERT INTO walking_award(id, tenant_id, activity_id, award_type, award_name, rank_start, rank_end, prize_content, image_url, status, sort_order, del_flag, create_time) VALUES
(10001, '000000', 1001, 1, '一等奖',     1,  5,  '荣誉证书 + 价值 100 元健身礼包', '', 1, 1, 0, sysdate()),
(10002, '000000', 1001, 1, '二等奖',     6,  15, '荣誉证书 + 价值 80 元健身礼包',  '', 1, 2, 0, sysdate()),
(10003, '000000', 1001, 1, '三等奖',     16, 35, '荣誉证书 + 价值 50 元健身礼包',  '', 1, 3, 0, sysdate()),
(10004, '000000', 1001, 1, '优秀奖',     36, 0,  '荣誉证书 + 纪念品一份',          '', 1, 4, 0, sysdate()),
(10005, '000000', 1001, 2, '先进组织单位', 1, 5,  '奖牌 + 奖金 1000 元',            '', 1, 5, 0, sysdate());
