# 健步同行多端活动管理平台

基于 Spring Boot 与 Vue 3 的线上健步走活动管理系统，包含后台管理端、微信小程序、H5 报名页和业务文档。

## 项目结构

- `RuoYi-Vue-Plus-5.6.0`：Spring Boot 后端
- `plus-ui-6.X-Vue`：Vue 3 后台管理端
- `jianbu-app`：uni-app 微信小程序
- `jianbu-h5`：H5 报名页
- `jianbu-docs`：设计文档与数据库脚本

## 核心功能

- 活动配置与报名审核
- 微信步数同步、积分与排名
- 组织机构与会员管理
- 异常数据识别与审计
- 奖励配置与中奖名单管理
- 角色、按钮和管理员级权限控制

## 技术栈

Java、Spring Boot、MyBatis-Plus、Sa-Token、MySQL、Redis、Vue 3、TypeScript、Element Plus、uni-app。

详细部署步骤见 `jianbu-docs/05-部署文档.md`。微信 AppID、AppSecret 与数据库凭据请通过环境变量配置。
