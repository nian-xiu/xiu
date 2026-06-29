# 阿里云 ECS Docker 部署说明

这份说明适合一台全新的 Linux ECS。项目会用 Docker Compose 启动两个容器：`ssm-shop-app` 和 `ssm-shop-mysql`。

## 你需要准备的信息

- ECS 公网 IP。
- ECS 登录方式：root 密码或 SSH 私钥。
- GitHub 仓库地址：`https://github.com/nian-xiu/xiu.git`。
- 一个生产环境密码，用于 MySQL root 和应用连接数据库。

## 1. 在阿里云控制台开放端口

1. 打开阿里云控制台，进入「云服务器 ECS」。
2. 左侧进入「实例与镜像」->「实例」。
3. 找到你的服务器，点击实例 ID。
4. 进入「安全组」页签，点击对应安全组的「配置规则」。
5. 在「入方向」添加规则：
   - 协议类型：`TCP`
   - 端口范围：`8080/8080`
   - 授权对象：测试阶段可填 `0.0.0.0/0`
   - 授权策略：允许
   - 优先级：默认即可
6. 如果 SSH 端口没有开放，也添加：
   - 协议类型：`TCP`
   - 端口范围：`22/22`
   - 授权对象：建议填你自己的公网 IP，例如 `你的IP/32`

> 后续如果配置域名和 HTTPS，建议只对公网开放 `80` 和 `443`，把 `8080` 收回。

## 2. SSH 登录服务器

在本地 Windows PowerShell 中执行：

```powershell
ssh root@你的ECS公网IP
```

如果使用私钥：

```powershell
ssh -i C:\Users\你的用户名\Downloads\你的私钥.pem root@你的ECS公网IP
```

首次连接出现确认提示时，输入 `yes` 回车，然后输入服务器密码或使用私钥登录。

## 3. 安装 Docker 和 Compose

先判断系统：

```bash
cat /etc/os-release
```

Ubuntu 系统执行：

```bash
apt update
apt install -y ca-certificates curl git
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc
. /etc/os-release
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu ${VERSION_CODENAME} stable" > /etc/apt/sources.list.d/docker.list
apt update
apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
systemctl enable --now docker
docker --version
docker compose version
```

Debian 系统执行：

```bash
apt update
apt install -y ca-certificates curl git
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc
. /etc/os-release
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian ${VERSION_CODENAME} stable" > /etc/apt/sources.list.d/docker.list
apt update
apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
systemctl enable --now docker
docker --version
docker compose version
```

Alibaba Cloud Linux / CentOS / Rocky Linux 系统执行：

```bash
yum install -y yum-utils git
yum-config-manager --add-repo http://mirrors.cloud.aliyuncs.com/docker-ce/linux/centos/docker-ce.repo
yum install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
systemctl enable --now docker
docker --version
docker compose version
```

## 4. 拉取项目代码

```bash
mkdir -p /opt/ssm-shop
cd /opt/ssm-shop
git clone https://github.com/nian-xiu/xiu.git .
```

如果目录不是空的，可以改用：

```bash
cd /opt/ssm-shop
git pull
```

## 5. 配置环境变量

```bash
cp .env.example .env
nano .env
```

把 `.env` 中这些值改掉：

```env
MYSQL_ROOT_PASSWORD=换成一个强密码
APP_PASSWORD_SALT=换成一串足够长的随机字符串
APP_PORT=8080
```

保存 nano：按 `Ctrl + O`，回车，再按 `Ctrl + X` 退出。

## 6. 启动项目

```bash
docker compose up -d --build
```

查看状态：

```bash
docker compose ps
```

查看日志：

```bash
docker compose logs -f app
```

浏览器访问：

```text
http://你的ECS公网IP:8080/
http://你的ECS公网IP:8080/admin
```

演示账号：

- 普通用户：`customer` / `123456`
- 管理员：`admin` / `admin123`

## 常用维护命令

更新代码并重新部署：

```bash
cd /opt/ssm-shop
git pull
docker compose up -d --build
```

停止项目：

```bash
docker compose down
```

重启项目：

```bash
docker compose restart
```

备份数据库：

```bash
docker exec ssm-shop-mysql mysqldump -uroot -p ssm_shop > ssm_shop_backup.sql
```

进入数据库：

```bash
docker exec -it ssm-shop-mysql mysql -uroot -p ssm_shop
```

## 排错

如果浏览器打不开：

1. 确认安全组已经开放 `8080`。
2. 执行 `docker compose ps`，确认 `app` 和 `mysql` 都是运行状态。
3. 执行 `docker compose logs --tail=100 app` 查看应用日志。
4. 执行 `curl -I http://127.0.0.1:8080/`，判断服务器本机是否能访问。
5. 如果服务器本机能访问、公网不能访问，通常是安全组或系统防火墙问题。
