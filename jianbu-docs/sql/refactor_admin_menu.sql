-- =============================================================
-- 健步走后台改造: 用户管理→会员管理 + 角色管理→管理员身份管理
-- 目标库: ry-vue
-- 前置: 已执行 cleanup_hide_menus.sql(隐藏无关菜单)
-- 说明:
--   1. 系统管理"用户管理"(menu 100)改为展示工会会员(walking_member)
--   2. 系统管理"角色管理"(menu 101)改为"管理员身份管理"(新页面)
--   3. 健步走管理下 4003 会员管理删除(避免重复)
--   4. 管理员角色(101)补"停用账号/撤下报名"权限
-- 执行: mysql --default-character-set=utf8mb4 -uroot -p123456 ry-vue < refactor_admin_menu.sql
-- =============================================================

-- -------------------------------------------------------------
-- 一、目标B: 用户管理 → 会员管理
-- -------------------------------------------------------------

-- 100 用户管理菜单改名+指向会员页
UPDATE sys_menu SET menu_name='会员管理', component='walking/member/index', perms='walking:member:list' WHERE menu_id=100;

-- 会员按钮换挂到 100 下(原挂在 4003 下)
UPDATE sys_menu SET parent_id=100, order_num=3 WHERE menu_id=40031;
UPDATE sys_menu SET parent_id=100, order_num=4 WHERE menu_id=40032;
UPDATE sys_menu SET parent_id=100, order_num=5 WHERE menu_id=40033;
UPDATE sys_menu SET parent_id=100, order_num=6 WHERE menu_id=40034;

-- 新增会员增删按钮(仅系统管理员)
INSERT INTO sys_menu VALUES(10031, '会员新增', 100, 1, '', '', '', 1, 0, 'F', '0', '0', 'walking:member:add', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES(10032, '会员删除', 100, 2, '', '', '', 1, 0, 'F', '0', '0', 'walking:member:remove', '#', 103, 1, sysdate(), NULL, NULL, '');

-- 删除健步走管理下 4003 会员管理(避免重复)
DELETE FROM sys_role_menu WHERE menu_id=4003;
DELETE FROM sys_menu WHERE menu_id=4003;

-- -------------------------------------------------------------
-- 二、目标C: 角色管理 → 管理员身份管理
-- -------------------------------------------------------------

-- 101 角色管理菜单改名+指向新页面
UPDATE sys_menu SET menu_name='管理员身份管理', component='system/role/identity' WHERE menu_id=101;

-- 用户管理按钮换挂到 101 下(管理员身份管理复用 system:user:* 权限)
UPDATE sys_menu SET parent_id=101, order_num=1 WHERE menu_id=1001;
UPDATE sys_menu SET parent_id=101, order_num=2 WHERE menu_id=1002;
UPDATE sys_menu SET parent_id=101, order_num=3 WHERE menu_id=1003;
UPDATE sys_menu SET parent_id=101, order_num=4 WHERE menu_id=1004;
UPDATE sys_menu SET parent_id=101, order_num=5 WHERE menu_id=1007;
UPDATE sys_menu SET parent_id=101, order_num=6 WHERE menu_id=1008;
UPDATE sys_menu SET parent_id=101, order_num=7 WHERE menu_id=1010;

-- 角色新增/删除按钮(1009/1011)保留但不授权,实现"固定两种身份"

-- -------------------------------------------------------------
-- 三、角色权限调整
-- -------------------------------------------------------------

-- 系统管理员(100): 补 系统管理目录/会员管理/管理员身份管理 及按钮
INSERT INTO sys_role_menu(role_id, menu_id) VALUES
 (100, 1), (100, 100), (100, 10031), (100, 10032), (100, 101),
 (100, 1001), (100, 1002), (100, 1003), (100, 1004), (100, 1007),
 (100, 1008), (100, 1010);

-- 管理员(101): 补"可停用异常报名/账号" + 会员管理可见
INSERT INTO sys_role_menu(role_id, menu_id) VALUES
 (101, 1), (101, 100), (101, 40033), (101, 40023);

-- -------------------------------------------------------------
-- 四、可选: 组织机构会员总数一次性回填(历史兼容)
-- -------------------------------------------------------------
-- UPDATE sys_dept SET walking_member_total =
--   (SELECT COUNT(*) FROM walking_member WHERE dept_id=sys_dept.dept_id AND del_flag=0)
-- WHERE dept_category='union';
