-- =============================================================
-- 健步走线上活动系统 后台管理 —— 角色与权限初始化脚本
-- 目标库: ry-vue
-- 内容: 菜单(sys_menu) + 角色(sys_role) + 角色-菜单(sys_role_menu)
-- 角色说明:
--   [系统管理员] walking_super_admin: 全部功能、全部数据
--       含 活动参数配置/批量导入组织机构/批量处理作弊账号/分单位导出
--   [管理员] walking_admin: 日常管理
--       不含 审核类(停用账号/撤下报名) 与 系统管理类(参数配置/批量导入/导出/批量作弊)
--       这两类为按钮级权限, 系统管理员可按账号勾选分配(权限细分)
-- 注意: 框架内置"超级管理员"(role_id=1) 不受权限校验限制, 天然拥有全部功能
-- 执行: mysql --default-character-set=utf8mb4 -uroot -p123456 ry-vue < walking_admin_menu.sql
-- =============================================================

-- -------------------------------------------------------------
-- 1. 菜单权限 sys_menu
--    目录: 4000 健步走管理
-- -------------------------------------------------------------

-- 一级目录
INSERT INTO sys_menu VALUES(4000, '健步走管理', 0, 10, 'walking', NULL, '', 1, 0, 'M', '0', '0', '', 'flag', 103, 1, sysdate(), NULL, NULL, '健步走管理目录');

-- 4001 活动管理
INSERT INTO sys_menu VALUES(4001, '活动管理', 4000, 1, 'activity', 'walking/activity/index', '', 1, 0, 'C', '0', '0', 'walking:activity:list', 'flag', 103, 1, sysdate(), NULL, NULL, '活动管理菜单');
INSERT INTO sys_menu VALUES(40011, '活动查询', 4001, 1, '', '', '', 1, 0, 'F', '0', '0', 'walking:activity:query', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40012, '活动新增', 4001, 2, '', '', '', 1, 0, 'F', '0', '0', 'walking:activity:add', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40013, '活动修改', 4001, 3, '', '', '', 1, 0, 'F', '0', '0', 'walking:activity:edit', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40014, '活动删除', 4001, 4, '', '', '', 1, 0, 'F', '0', '0', 'walking:activity:remove', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40015, '活动参数配置', 4001, 5, '', '', '', 1, 0, 'F', '0', '0', 'walking:activity:config', '#', 103, 1, sysdate(), NULL, NULL, '系统管理员专属: 积分规则/时间窗口等参数');

-- 4002 报名管理
INSERT INTO sys_menu VALUES(4002, '报名管理', 4000, 2, 'registration', 'walking/registration/index', '', 1, 0, 'C', '0', '0', 'walking:registration:list', 'form', 103, 1, sysdate(), NULL, NULL, '报名管理菜单');
INSERT INTO sys_menu VALUES(40021, '报名查询', 4002, 1, '', '', '', 1, 0, 'F', '0', '0', 'walking:registration:query', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40022, '取消报名', 4002, 2, '', '', '', 1, 0, 'F', '0', '0', 'walking:registration:cancel', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40023, '撤下/停用报名', 4002, 3, '', '', '', 1, 0, 'F', '0', '0', 'walking:registration:disable', '#', 103, 1, sysdate(), NULL, NULL, '审核类权限: 撤下报名信息并停用会员账号, 默认不分配');
INSERT INTO sys_menu VALUES(40024, '调整单位', 4002, 4, '', '', '', 1, 0, 'F', '0', '0', 'walking:registration:adjust', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40025, '分单位导出报名', 4002, 5, '', '', '', 1, 0, 'F', '0', '0', 'walking:registration:export', '#', 103, 1, sysdate(), NULL, NULL, '系统管理员专属: 分单位导出报名人员信息');
INSERT INTO sys_menu VALUES(40026, '报名审核', 4002, 6, '', '', '', 1, 0, 'F', '0', '0', 'walking:registration:approve', '#', 103, 1, sysdate(), NULL, NULL, '审核报名通过或驳回');

-- 4003 会员管理
INSERT INTO sys_menu VALUES(4003, '会员管理', 4000, 3, 'member', 'walking/member/index', '', 1, 0, 'C', '0', '0', 'walking:member:list', 'peoples', 103, 1, sysdate(), NULL, NULL, '会员管理菜单');
INSERT INTO sys_menu VALUES(40031, '会员查询', 4003, 1, '', '', '', 1, 0, 'F', '0', '0', 'walking:member:query', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40032, '会员编辑', 4003, 2, '', '', '', 1, 0, 'F', '0', '0', 'walking:member:edit', '#', 103, 1, sysdate(), NULL, NULL, '维护单位/收货地址等');
INSERT INTO sys_menu VALUES(40033, '停用账号', 4003, 3, '', '', '', 1, 0, 'F', '0', '0', 'walking:member:disable', '#', 103, 1, sysdate(), NULL, NULL, '审核类权限: 停用会员账号, 默认不分配');
INSERT INTO sys_menu VALUES(40034, '导出打卡信息', 4003, 4, '', '', '', 1, 0, 'F', '0', '0', 'walking:member:export', '#', 103, 1, sysdate(), NULL, NULL, '系统管理员专属: 分单位导出会员打卡信息');

-- 4004 组织机构管理
INSERT INTO sys_menu VALUES(4004, '组织机构管理', 4000, 4, 'org', 'walking/org/index', '', 1, 0, 'C', '0', '0', 'walking:org:list', 'tree', 103, 1, sysdate(), NULL, NULL, '组织机构管理菜单(复用sys_dept)');
INSERT INTO sys_menu VALUES(40041, '机构查询', 4004, 1, '', '', '', 1, 0, 'F', '0', '0', 'walking:org:query', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40042, '机构新增', 4004, 2, '', '', '', 1, 0, 'F', '0', '0', 'walking:org:add', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40043, '机构修改', 4004, 3, '', '', '', 1, 0, 'F', '0', '0', 'walking:org:edit', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40044, '机构删除', 4004, 4, '', '', '', 1, 0, 'F', '0', '0', 'walking:org:remove', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40045, '批量导入机构', 4004, 5, '', '', '', 1, 0, 'F', '0', '0', 'walking:org:import', '#', 103, 1, sysdate(), NULL, NULL, '系统管理员专属: 批量导入组织机构');

-- 4005 数据统计
INSERT INTO sys_menu VALUES(4005, '数据统计', 4000, 5, 'stats', 'walking/stats/index', '', 1, 0, 'C', '0', '0', 'walking:stats:list', 'chart', 103, 1, sysdate(), NULL, NULL, '数据统计菜单');
INSERT INTO sys_menu VALUES(40051, '统计查询', 4005, 1, '', '', '', 1, 0, 'F', '0', '0', 'walking:stats:query', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40052, '分单位导出', 4005, 2, '', '', '', 1, 0, 'F', '0', '0', 'walking:stats:export', '#', 103, 1, sysdate(), NULL, NULL, '系统管理员专属: 分单位导出报名+打卡信息');

-- 4006 异常数据处理
INSERT INTO sys_menu VALUES(4006, '异常数据处理', 4000, 6, 'cheat', 'walking/cheat/index', '', 1, 0, 'C', '0', '0', 'walking:cheat:list', 'warning', 103, 1, sysdate(), NULL, NULL, '异常数据处理菜单');
INSERT INTO sys_menu VALUES(40061, '异常数据查询', 4006, 1, '', '', '', 1, 0, 'F', '0', '0', 'walking:cheat:query', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40062, '标记异常', 4006, 2, '', '', '', 1, 0, 'F', '0', '0', 'walking:cheat:mark', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40063, '删除异常数据', 4006, 3, '', '', '', 1, 0, 'F', '0', '0', 'walking:cheat:delete', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40064, '批量处理作弊', 4006, 4, '', '', '', 1, 0, 'F', '0', '0', 'walking:cheat:batch', '#', 103, 1, sysdate(), NULL, NULL, '系统管理员专属: 批量标记/删除/停用/取消');

-- 4007 审核日志
INSERT INTO sys_menu VALUES(4007, '审核日志', 4000, 7, 'audit', 'walking/audit/index', '', 1, 0, 'C', '0', '0', 'walking:audit:list', 'log', 103, 1, sysdate(), NULL, NULL, '审核日志菜单');
INSERT INTO sys_menu VALUES(40071, '日志查询', 4007, 1, '', '', '', 1, 0, 'F', '0', '0', 'walking:audit:query', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(40001, '管理员权限配置', 4000, 8, '', '', '', 1, 0, 'F', '0', '0', 'walking:userPerm:edit', '#', 103, 1, sysdate(), NULL, NULL, '系统管理员专属: 配置管理员的健步走菜单与按钮权限');

-- -------------------------------------------------------------
-- 2. 角色 sys_role
--    使用独立角色ID 100 / 101, 与框架内置角色(1/3/4)不冲突
-- -------------------------------------------------------------
INSERT INTO sys_role VALUES(100, '000000', '系统管理员', 'walking_super_admin', 1, 1, 1, 1, '0', '0', 103, 1, sysdate(), NULL, NULL, '健步走系统管理员: 全部功能、全部数据、活动参数配置、批量导入机构、批量处理作弊、分单位导出');
INSERT INTO sys_role VALUES(101, '000000', '管理员', 'walking_admin', 2, 1, 1, 1, '0', '0', 103, 1, sysdate(), NULL, NULL, '健步走日常管理员: 活动/报名/会员/机构/统计/异常处理; 审核类与系统管理类权限按需勾选');

-- -------------------------------------------------------------
-- 3. 角色-菜单 sys_role_menu
-- -------------------------------------------------------------

-- 3.1 系统管理员(walking_super_admin): 全部健步走菜单(含系统管理类/审核类按钮)
INSERT INTO sys_role_menu VALUES(100, 4000);
INSERT INTO sys_role_menu VALUES(100, 4001);
INSERT INTO sys_role_menu VALUES(100, 40011);
INSERT INTO sys_role_menu VALUES(100, 40012);
INSERT INTO sys_role_menu VALUES(100, 40013);
INSERT INTO sys_role_menu VALUES(100, 40014);
INSERT INTO sys_role_menu VALUES(100, 40015);
INSERT INTO sys_role_menu VALUES(100, 4002);
INSERT INTO sys_role_menu VALUES(100, 40021);
INSERT INTO sys_role_menu VALUES(100, 40022);
INSERT INTO sys_role_menu VALUES(100, 40023);
INSERT INTO sys_role_menu VALUES(100, 40024);
INSERT INTO sys_role_menu VALUES(100, 40025);
INSERT INTO sys_role_menu VALUES(100, 40026);
INSERT INTO sys_role_menu VALUES(100, 4003);
INSERT INTO sys_role_menu VALUES(100, 40031);
INSERT INTO sys_role_menu VALUES(100, 40032);
INSERT INTO sys_role_menu VALUES(100, 40033);
INSERT INTO sys_role_menu VALUES(100, 40034);
INSERT INTO sys_role_menu VALUES(100, 4004);
INSERT INTO sys_role_menu VALUES(100, 40041);
INSERT INTO sys_role_menu VALUES(100, 40042);
INSERT INTO sys_role_menu VALUES(100, 40043);
INSERT INTO sys_role_menu VALUES(100, 40044);
INSERT INTO sys_role_menu VALUES(100, 40045);
INSERT INTO sys_role_menu VALUES(100, 4005);
INSERT INTO sys_role_menu VALUES(100, 40051);
INSERT INTO sys_role_menu VALUES(100, 40052);
INSERT INTO sys_role_menu VALUES(100, 4006);
INSERT INTO sys_role_menu VALUES(100, 40061);
INSERT INTO sys_role_menu VALUES(100, 40062);
INSERT INTO sys_role_menu VALUES(100, 40063);
INSERT INTO sys_role_menu VALUES(100, 40064);
INSERT INTO sys_role_menu VALUES(100, 4007);
INSERT INTO sys_role_menu VALUES(100, 40071);
INSERT INTO sys_role_menu VALUES(100, 40001);

-- 3.2 管理员(walking_admin): 日常管理菜单
--     不含: 40015 活动参数配置 / 40023 撤下停用报名(审核类) / 40025 导出报名 /
--           40033 停用账号(审核类) / 40034 导出打卡 / 40045 批量导入机构 /
--           40052 分单位导出 / 40064 批量处理作弊
INSERT INTO sys_role_menu VALUES(101, 4000);
INSERT INTO sys_role_menu VALUES(101, 4001);
INSERT INTO sys_role_menu VALUES(101, 40011);
INSERT INTO sys_role_menu VALUES(101, 40012);
INSERT INTO sys_role_menu VALUES(101, 40013);
INSERT INTO sys_role_menu VALUES(101, 40014);
INSERT INTO sys_role_menu VALUES(101, 4002);
INSERT INTO sys_role_menu VALUES(101, 40021);
INSERT INTO sys_role_menu VALUES(101, 40022);
INSERT INTO sys_role_menu VALUES(101, 40024);
INSERT INTO sys_role_menu VALUES(101, 4003);
INSERT INTO sys_role_menu VALUES(101, 40031);
INSERT INTO sys_role_menu VALUES(101, 40032);
INSERT INTO sys_role_menu VALUES(101, 4004);
INSERT INTO sys_role_menu VALUES(101, 40041);
INSERT INTO sys_role_menu VALUES(101, 40042);
INSERT INTO sys_role_menu VALUES(101, 40043);
INSERT INTO sys_role_menu VALUES(101, 40044);
INSERT INTO sys_role_menu VALUES(101, 4005);
INSERT INTO sys_role_menu VALUES(101, 40051);
INSERT INTO sys_role_menu VALUES(101, 4006);
INSERT INTO sys_role_menu VALUES(101, 40061);
INSERT INTO sys_role_menu VALUES(101, 40062);
INSERT INTO sys_role_menu VALUES(101, 40063);
INSERT INTO sys_role_menu VALUES(101, 4007);
INSERT INTO sys_role_menu VALUES(101, 40071);
