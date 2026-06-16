# 心怡商城

基于 Spring Boot 3 + Spring MVC + MyBatis 的网上商城演示系统。内嵌 Undertow，无需外部 Tomcat。

## 技术栈

- JDK 21
- Spring Boot 3.5.7
- Spring MVC + Thymeleaf
- MyBatis XML Mapper
- MySQL 9
- Undertow
- Maven

## 功能模块

- **前台商城**：首页推荐、分类导航、商品搜索、价格筛选、销量/评分/价格排序、商品详情
- **用户功能**：注册、登录、退出、收藏、购物车、地址管理、订单提交、订单查看
- **交易流程**：购物车结算、库存校验、下单扣库存、销量累计、订单状态流转、金币抵扣、优惠券使用
- **活动 / 福利**（本次重点优化）
  - 公开活动：金币 / 折扣券 / 满减券，支持限时秒杀大卡 + 倒计时
  - 奖励邮件：定向单发 + 群发所有活跃用户，邮件页支持「一键全部领取」
  - 兑换码（CDKEY）：管理员可指定或随机生成，用户在背包页输码即可入账
  - 新人首单券：注册即自动发放满 100 减 20
  - 后台支持活动「启用 / 暂停 / 克隆 / 删除」，兑换码同样可启用 / 停用 / 删除
- **后台管理**：仪表盘、商品 CRUD、订单状态、客服消息、用户启用/停用、活动 / 邮件 / 兑换码运营中心
- **数据初始化**：首次启动建表 + 写入演示数据；之后由 `app_meta.schema_initialized` 标记跳过，启动 < 2 秒

## 运行前准备

```sql
CREATE DATABASE ssm_shop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

数据库配置位于 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    username: root
    password: 123456
```

## 启动项目

```bash
mvn spring-boot:run
```

访问：

- 商城首页：http://localhost:8080/
- 后台首页：http://localhost:8080/admin

演示账号：

- 普通用户：`customer` / `123456`
- 管理员：`admin` / `admin123`

## 性能与启动优化

- `spring.sql.init.mode` 关闭，统一由 `DataSeeder` 在首次启动执行 `schema.sql + data.sql`，并通过 `app_meta.schema_initialized` 标记跳过后续运行
- 升级老库时，`DataSeeder.runMigrations` 会幂等修补 `discount_rate` 可空、`flash_sale` 列等历史结构差异，无须手动执行
- 移除 `spring-boot-devtools`，关闭 banner / JMX / livereload，启动日志降到 WARN 级别
- Thymeleaf 缓存开启（生产模式），HikariCP 连接池上限 8，Undertow IO 4 线程 + Worker 16
- 静态 HTML / CSS / JS / SVG 自动 gzip 压缩

实测冷启动 ≈ **1.9 秒**（旧版 5–10 秒）。

## 安全修复

- 表单全部使用服务端注入的 `_csrf` 隐藏字段（`fragments/layout :: csrf` 片段），不再仅靠 JS 注入兜底
- `CartService.update` 新增 `userId` 参数，避免越权改他人购物车
- `user_coupons.discount_rate` 改为可空，修复管理员发放「满减券」时的 SQL 约束错误
- `AuthController#safeRedirect` 白名单同步覆盖新增的 `/redeem`、`/mail/claim-all`、`/admin/activities/...`
- 文件上传保留 MIME / 后缀 / 文件头三重校验，防路径穿越
- 注册自动发放福利失败不阻断注册主流程

## 表结构（活动 / 邮件 / 兑换码 部分）

| 表名 | 作用 |
| --- | --- |
| `activity_campaigns` | 公开活动，新增 `flash_sale` 标记限时秒杀 |
| `activity_claims` | 用户领取记录，唯一约束防重复领取 |
| `reward_mails` | 站内信，支持金币 / 优惠券奖励 |
| `coupon_codes` | 兑换码主表，含名额与到期 |
| `coupon_code_redemptions` | 兑换记录，唯一约束防重复兑换 |
| `user_coupons` | 用户优惠券库存（背包） |

## 目录约定

- 商品封面图存放在 `app.upload.product-image-dir`（默认 `uploads/products`）
- 启动日志写入 `app.start.out.log` / `app.start.err.log`（如使用 `nohup` 后台启动）
