# 心悦商城

基于 Spring Boot 3、Spring MVC、MyBatis 和 Thymeleaf 的在线商城演示项目，内嵌 Undertow，无需额外部署 Tomcat。

## 技术栈

- Java 21
- Spring Boot 3.5.7
- Spring MVC + Thymeleaf
- MyBatis XML Mapper
- MySQL 8.0 / 9.x
- Undertow
- Maven

## 功能模块

- 前台商城：首页推荐、分类浏览、商品搜索、价格筛选、销量和评分排序、商品详情
- 用户功能：注册、登录、退出、收藏、购物车、地址管理、订单提交、订单查询
- 交易流程：购物车结算、库存校验、下单扣减库存、销量累计、订单状态流转、金币抵扣、优惠券使用
- 活动与福利：公开活动、邮件奖励、兑换码、首单券、后台活动运营
- 后台管理：仪表盘、商品管理、订单管理、公告管理、用户管理、客服消息、奖励中心
- 数据初始化：首次启动自动建表并写入演示数据，后续启动自动跳过

## 项目结构

```text
src/main/java                Java 源码
src/main/resources/templates Thymeleaf 页面模板
src/main/resources/static    静态资源
src/main/resources/db        建表与演示数据 SQL
src/main/resources/mapper    MyBatis XML
tools/                       辅助脚本
uploads/products/            商品图片资源
```

## 运行前准备

1. 创建数据库：

```sql
CREATE DATABASE ssm_shop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

2. 配置数据库连接。

项目默认从环境变量读取数据库配置；如果未设置，则使用下面这些默认值：

- `DB_URL`: `jdbc:mysql://localhost:3306/ssm_shop?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false&rewriteBatchedStatements=true`
- `DB_USERNAME`: `root`
- `DB_PASSWORD`: `change-me`
- `APP_PASSWORD_SALT`: `ssm-shop-demo-salt`

PowerShell 示例：

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的数据库密码"
mvn spring-boot:run
```

## 启动项目

开发运行：

```bash
mvn spring-boot:run
```

打包运行：

```bash
mvn package -DskipTests
java -Djava.io.tmpdir=tmp -jar target/ssm-shop-1.0.0.jar
```

Windows 下也可以直接使用：

```powershell
powershell -ExecutionPolicy Bypass -File tools/run-app.ps1
```

## 访问地址

- 商城首页：http://localhost:8080/
- 后台入口：http://localhost:8080/admin

## 演示账号

- 普通用户：`customer` / `123456`
- 管理员：`admin` / `admin123`

## 配置说明

- `spring.sql.init.mode=never`，数据库初始化统一由 `DataSeeder` 负责
- 首次启动会执行 `schema.sql` 和 `data.sql`
- 通过 `app_meta.schema_initialized` 标记避免重复初始化
- 启用了 Thymeleaf 缓存、Undertow 压缩和连接池参数优化

## 安全与协作建议

- 不要把真实数据库密码直接写入仓库中的 `application.yml`
- 推荐通过环境变量或未纳入版本控制的本地配置文件覆盖默认值
- 当前仓库已忽略构建产物、临时目录和运行日志

## License

本项目采用 [MIT License](./LICENSE)。
