-- =============================================================
-- 健步走线上活动系统 建表脚本
-- 目标库: ry-vue  表前缀: walking_
-- 约定: 主键 bigint 雪花ID(应用生成,非自增), 逻辑删除 del_flag
-- 执行: mysql --default-character-set=utf8mb4 -uroot -p123456 ry-vue < walking_tables.sql
-- =============================================================

-- 1. 活动表
CREATE TABLE `walking_activity` (
  `id` bigint NOT NULL COMMENT '主键',
  `tenant_id` varchar(20) DEFAULT '000000' COMMENT '租户id',
  `activity_name` varchar(100) NOT NULL COMMENT '活动名称',
  `cover_url` varchar(500) DEFAULT '' COMMENT '封面图地址',
  `description` text COMMENT '活动介绍',
  `rule_content` text COMMENT '活动规则说明',
  `start_date` date DEFAULT NULL COMMENT '活动开始日期',
  `end_date` date DEFAULT NULL COMMENT '活动结束日期',
  `target_steps` int DEFAULT '0' COMMENT '活动目标总步数',
  `daily_target_steps` int DEFAULT '0' COMMENT '每日目标步数',
  `points_per_thousand_steps` int DEFAULT '0' COMMENT '每千步积分',
  `register_start` datetime DEFAULT NULL COMMENT '报名开始时间',
  `register_end` datetime DEFAULT NULL COMMENT '报名截止时间',
  `status` int DEFAULT '0' COMMENT '状态(0草稿 1进行中 2已结束)',
  `org_id` bigint DEFAULT NULL COMMENT '主办单位id(关联sys_dept.dept_id)',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `del_flag` int DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_act_status` (`status`),
  KEY `idx_act_dates` (`start_date`,`end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='健步走活动表';

-- 2. 会员表（小程序用户/工会会员）
CREATE TABLE `walking_member` (
  `id` bigint NOT NULL COMMENT '主键',
  `tenant_id` varchar(20) DEFAULT '000000' COMMENT '租户id',
  `openid` varchar(64) DEFAULT NULL COMMENT '微信openid',
  `unionid` varchar(64) DEFAULT NULL COMMENT '微信unionid',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `real_name` varchar(50) DEFAULT '' COMMENT '姓名',
  `id_card` varchar(30) DEFAULT '' COMMENT '身份证号',
  `dept_id` bigint DEFAULT NULL COMMENT '所属单位(关联sys_dept.dept_id)',
  `dept_name` varchar(100) DEFAULT '' COMMENT '单位名称(冗余)',
  `avatar` varchar(500) DEFAULT '' COMMENT '头像地址',
  `status` int DEFAULT '0' COMMENT '状态(0正常 1停用)',
  `is_verified` int DEFAULT '0' COMMENT '是否审核锁定(0否 1是,锁定后姓名/手机号不可改)',
  `register_time` datetime DEFAULT NULL COMMENT '注册/首次登录时间',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `del_flag` int DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_openid` (`openid`),
  UNIQUE KEY `uk_member_phone` (`phone`),
  KEY `idx_member_dept` (`dept_id`),
  KEY `idx_member_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='健步走会员表';

-- 3. 报名表（报名+状态机）
CREATE TABLE `walking_registration` (
  `id` bigint NOT NULL COMMENT '主键',
  `tenant_id` varchar(20) DEFAULT '000000' COMMENT '租户id',
  `member_id` bigint NOT NULL COMMENT '会员id',
  `activity_id` bigint NOT NULL COMMENT '活动id',
  `status` int DEFAULT '0' COMMENT '状态(0待提交草稿 1待审核 2审核通过 3已取消 4已停用撤下)',
  `audit_result` varchar(500) DEFAULT '' COMMENT '审核结果/异常原因(含敏感词提示)',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `cancel_by` varchar(50) DEFAULT '' COMMENT '取消/撤下操作人',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消/撤下时间',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `del_flag` int DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reg_member_activity` (`member_id`,`activity_id`),
  KEY `idx_reg_status` (`status`),
  KEY `idx_reg_activity` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='健步走报名表';

-- 4. 步数打卡表
CREATE TABLE `walking_step_record` (
  `id` bigint NOT NULL COMMENT '主键',
  `tenant_id` varchar(20) DEFAULT '000000' COMMENT '租户id',
  `member_id` bigint NOT NULL COMMENT '会员id',
  `activity_id` bigint NOT NULL COMMENT '活动id',
  `record_date` date NOT NULL COMMENT '打卡日期',
  `steps` int DEFAULT '0' COMMENT '步数',
  `source` int DEFAULT '1' COMMENT '来源(1微信运动 2人工/管理员调整)',
  `abnormal_flag` int DEFAULT '0' COMMENT '异常标志(0正常 1疑似作弊)',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `del_flag` int DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_step_member_date` (`member_id`,`activity_id`,`record_date`),
  KEY `idx_step_activity_date` (`activity_id`,`record_date`),
  KEY `idx_step_abnormal` (`abnormal_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='健步走步数打卡表';

-- 5. 积分表
CREATE TABLE `walking_points` (
  `id` bigint NOT NULL COMMENT '主键',
  `tenant_id` varchar(20) DEFAULT '000000' COMMENT '租户id',
  `member_id` bigint NOT NULL COMMENT '会员id',
  `activity_id` bigint NOT NULL COMMENT '活动id',
  `points` int DEFAULT '0' COMMENT '积分变动(正为加)',
  `reason` varchar(200) DEFAULT '' COMMENT '积分来源(步数达标/活动奖励等)',
  `record_date` date DEFAULT NULL COMMENT '发生日期',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `del_flag` int DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_points_member` (`member_id`,`activity_id`),
  KEY `idx_points_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='健步走积分表';

-- 6. 奖励表（奖励档位介绍）
CREATE TABLE `walking_award` (
  `id` bigint NOT NULL COMMENT '主键',
  `tenant_id` varchar(20) DEFAULT '000000' COMMENT '租户id',
  `activity_id` bigint NOT NULL COMMENT '活动id',
  `award_name` varchar(100) DEFAULT '' COMMENT '奖励名称',
  `rank_start` int DEFAULT '0' COMMENT '名次区间起',
  `rank_end` int DEFAULT '0' COMMENT '名次区间止',
  `prize_content` text COMMENT '奖励内容说明',
  `image_url` varchar(500) DEFAULT '' COMMENT '奖品图片地址',
  `status` int DEFAULT '0' COMMENT '状态(0停用 1启用)',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `del_flag` int DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_award_activity` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='健步走奖励表';

-- 7. 证书表（获奖证书）
CREATE TABLE `walking_certificate` (
  `id` bigint NOT NULL COMMENT '主键',
  `tenant_id` varchar(20) DEFAULT '000000' COMMENT '租户id',
  `member_id` bigint NOT NULL COMMENT '会员id',
  `activity_id` bigint NOT NULL COMMENT '活动id',
  `rank` int DEFAULT '0' COMMENT '名次',
  `cert_no` varchar(50) DEFAULT '' COMMENT '证书编号',
  `cert_title` varchar(100) DEFAULT '' COMMENT '证书标题',
  `template_url` varchar(500) DEFAULT '' COMMENT '证书模板/图片地址',
  `issue_time` datetime DEFAULT NULL COMMENT '颁发时间',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `del_flag` int DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cert_member_activity` (`member_id`,`activity_id`),
  KEY `idx_cert_activity_rank` (`activity_id`,`rank`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='健步走证书表';

-- 8. 作弊/异常处理审计表
CREATE TABLE `walking_cheat_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `tenant_id` varchar(20) DEFAULT '000000' COMMENT '租户id',
  `member_id` bigint NOT NULL COMMENT '会员id',
  `activity_id` bigint DEFAULT NULL COMMENT '活动id',
  `record_date` date DEFAULT NULL COMMENT '异常数据日期',
  `abnormal_type` varchar(50) DEFAULT '' COMMENT '异常类型(步数超限/数据突增/非活动期等)',
  `handle_type` int DEFAULT '1' COMMENT '处理方式(1标记异常 2删除数据 3停用账号 4取消报名)',
  `operator` varchar(50) DEFAULT '' COMMENT '处理管理员',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `del_flag` int DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_cheat_member` (`member_id`),
  KEY `idx_cheat_activity` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='健步走作弊处理审计表';

-- 9. 报名审核日志表
CREATE TABLE `walking_audit_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `tenant_id` varchar(20) DEFAULT '000000' COMMENT '租户id',
  `registration_id` bigint NOT NULL COMMENT '报名id',
  `member_id` bigint NOT NULL COMMENT '会员id',
  `audit_action` varchar(50) DEFAULT '' COMMENT '审核动作(提交/自动审核通过/自动审核拒绝/取消/撤下/停用)',
  `audit_result` varchar(500) DEFAULT '' COMMENT '审核结果/说明',
  `auditor` varchar(50) DEFAULT '' COMMENT '审核人(系统或管理员)',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `del_flag` int DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_audit_reg` (`registration_id`),
  KEY `idx_audit_member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='健步走报名审核日志表';

-- 10. 后台管理员用户级菜单覆盖配置
-- menu_id=0 为“已配置但无任何健步走权限”的内部标记。
CREATE TABLE `walking_user_menu` (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '后台管理员用户id',
  `menu_id` bigint NOT NULL COMMENT '允许访问的菜单id，0表示明确清空',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_walking_user_menu` (`user_id`,`menu_id`),
  KEY `idx_walking_user_menu_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='后台管理员健步走菜单权限覆盖表';
