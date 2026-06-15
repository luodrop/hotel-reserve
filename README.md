# HotelReserve — 酒店预约管理系统

基于 Spring Boot 的酒店房间预约管理系统，集成支付宝沙箱支付，支持内网穿透对外访问。

## 功能特性

- **用户模块**：注册、登录、个人信息管理
- **酒店浏览**：查看酒店列表、房间详情、房型与价格
- **预约管理**：在线预约房间、查看我的预约、取消预约
- **支付集成**：支付宝电脑网站支付（沙箱环境），支持扫码与登录支付
- **管理后台**：酒店管理、房间管理、订单管理、用户管理
- **内网穿透**：通过 natapp 将本地服务暴露到公网，便于支付宝回调与移动端访问

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.x |
| 构建工具 | Maven 3.9+ |
| 数据库 | MySQL 8.0+ |
| ORM | Spring Data JPA / Hibernate |
| 前端模板 | Thymeleaf + HTML/CSS/JavaScript |
| 支付接口 | 支付宝开放平台 — alipay.trade.page.pay |
| 内网穿透 | natapp (免费隧道) |
| Java 版本 | JDK 24 |

## 应用截图

> 以下截图均来自运行中的沙箱环境演示。

| 页面 | 截图 |
|------|------|
| 登录页 | ![登录页](docs/images/login-page.png) |
| 用户主页 | ![用户主页](docs/images/home-page.png) |
| 酒店详情 | ![酒店详情](docs/images/hotel-detail.png) |
| 预约弹窗 | ![预约弹窗](docs/images/booking-modal.png) |
| 填写预约信息 | ![填写预约](docs/images/booking-form.png) |
| 支付宝支付页 | ![支付页面](docs/images/payment-page.png) |
| 我的预约列表 | ![我的预约](docs/images/user-reservations.png) |

## 快速开始

### 前置要求

- JDK 24+
- Maven 3.9+
- MySQL 8.0+
- natapp 账号（可选，用于内网穿透）

### 1. 克隆项目

```bash
git clone https://github.com/luodrop/hotel-reserve.git
cd hotel-reserve
```

### 2. 创建数据库

```sql
CREATE DATABASE hotel_reservation CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 修改配置

编辑 `src/main/resources/application.properties`，根据你的环境修改：

```properties
# 数据库连接
spring.datasource.url=jdbc:mysql://localhost:3306/hotel_reservation?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=你的数据库密码

# 服务器端口（默认 80，如需内网穿透请保持一致）
server.port=80
```

### 4. 编译运行

```bash
mvn clean package -DskipTests
java -jar target/hotel-reserve-1.0.0.jar
```

启动后访问：`http://localhost:80`

### 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | `admin` | `admin123` |
| 普通用户 | `user` | `user123` |

---

## 支付宝沙箱配置教程

本项目使用支付宝开放平台的**沙箱环境**完成支付联调。沙箱环境与生产环境完全隔离，可免费模拟真实支付流程。

### 1. 登录沙箱控制台

访问 [支付宝沙箱控制台](https://open.alipay.com/develop/sandbox/account)，使用支付宝账号登录。

### 2. 获取沙箱应用信息

在沙箱控制台中找到以下信息：

- **APPID**（如 `9021000164658011`）
- **应用公钥 / 应用私钥**
- **支付宝公钥**
- **卖家 ID（seller_id）**

### 3. 生成 RSA 密钥对

支付宝推荐使用 RSA2 签名方式。可通过支付宝官方工具或 OpenSSL 生成 2048 位密钥：

```bash
# 生成私钥
openssl genrsa -out app_private_key.pem 2048

# 提取公钥
openssl rsa -in app_private_key.pem -pubout -out app_public_key.pem
```

### 4. 配置应用公钥

在沙箱控制台中点击"设置应用公钥"，将上一步生成的公钥内容粘贴进去并保存。

### 5. 配置支付宝公钥与私钥到项目

将生成的私钥与沙箱提供的支付宝公钥填入 `application.properties`：

```properties
alipay.app-id=9021000164658011
alipay.gateway-url=https://openapi-sandbox.dl.alipaydev.com/gateway.do
alipay.app-private-key=你的应用私钥（PKCS8 格式）
alipay.alipay-public-key=沙箱提供的支付宝公钥
alipay.sign-type=RSA2
alipay.charset=utf-8
alipay.seller-id=2088721102147645

# 回调地址（需使用 natapp 公网域名）
alipay.notify-url=http://你的natapp域名/callback/alipay/notify
alipay.return-url=http://你的natapp域名/callback/alipay/return
```

### 6. 沙箱测试账号

支付宝沙箱控制台会提供沙箱买家账号和登录密码。使用沙箱钱包 App 扫码或使用沙箱账号登录支付。

> **沙箱支付限制**：仅支持余额支付，不支持银行卡、余额宝、花呗等。沙箱环境会扣手续费，但比例不代表正式环境。

---

## Natapp 内网穿透配置

支付宝的回调接口（notify_url / return_url）要求公网可访问的地址。使用 natapp 可以将本地服务暴露到公网。

### 1. 注册 natapp

访问 [natapp.cn](https://natapp.cn) 注册账号。

### 2. 购买/创建隧道

在"我的隧道"中创建一条隧道：

| 配置项 | 值 |
|--------|-----|
| 隧道类型 | 免费型 |
| 隧道协议 | Web |
| 本地地址 | 127.0.0.1 |
| 本地端口 | **80**（与你应用的 `server.port` 一致） |
| 域名 | 系统随机分配 |

### 3. 下载 natapp 客户端

下载对应操作系统的客户端（Windows 选择 `natapp_windows_amd64`）。

### 4. 配置并启动

编写 `natapp.ini` 配置文件：

```ini
[default]
authtoken=你的authtoken
```

启动 natapp：

```bash
natapp.exe
```

启动后你将看到类似输出：

```
Tunnel Status           Online
Forwarding              http://xxxxx.natappfree.cc -> http://127.0.0.1:80
```

这个 `http://xxxxx.natappfree.cc` 就是你的公网域名，将 `application.properties` 中的 `alipay.notify-url` 和 `alipay.return-url` 替换为此域名。

### 5. 验证

浏览器访问 `http://xxxxx.natappfree.cc`，如果能看到你的应用，说明穿透配置成功。

---

## 支付宝接口说明

本项目使用以下支付宝接口：

| 接口 | 说明 |
|------|------|
| `alipay.trade.page.pay` | 电脑网站支付（统一收单下单并支付页面接口） |
| `alipay.trade.query` | 交易查询 |
| `alipay.trade.refund` | 交易退款 |
| `alipay.trade.close` | 交易关闭 |

沙箱注意事项：

- `timeout_express` / `time_expire` 不可超过当前时间 15 小时
- 沙箱不支持花呗分期测试
- 沙箱无法校验买家身份信息

---

## 项目结构

```
hotel-reserve/
├── docs/images/              # 应用截图
├── src/
│   ├── main/
│   │   ├── java/com/hotelreserve/
│   │   │   ├── config/           # 配置类（AlipayConfig、AuthInterceptor、WebConfig、SeedDataConfig）
│   │   │   ├── controller/       # 控制器（Admin、AlipayCallback、Auth、Home、User、Global）
│   │   │   ├── entity/           # 实体类（Hotel、Reservation、Room、User）
│   │   │   ├── repository/       # 数据访问层
│   │   │   ├── service/          # 业务逻辑层（AlipayService、SeedDataConfig）
│   │   │   └── HotelReserveApplication.java  # 入口
│   │   └── resources/
│   │       ├── static/           # 静态资源（css、js）
│   │       ├── templates/        # Thymeleaf 模板
│   │       │   ├── admin/        # 管理后台页面
│   │       │   ├── user/         # 用户页面
│   │       │   └── fragments/    # 公共模板片段
│   │       └── application.properties  # 全局配置
│   └── test/
├── .gitignore
├── pom.xml
└── README.md
```

## 许可证

MIT License