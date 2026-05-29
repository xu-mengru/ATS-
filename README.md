# 人事部招聘岗位发布系统 (ATS)

内部招聘岗位发布与管理系统，支持岗位增删改查、审批流程、统计分析和 Excel 批量导入。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.3 + Java 17 |
| 数据持久层 | Spring Data JPA |
| 数据库 | H2 (内存数据库，支持切换 MySQL) |
| Excel 处理 | EasyExcel 3.3 |
| 参数校验 | Hibernate Validator |
| 前端框架 | Vue 3 + Vite |
| UI 组件库 | Element Plus |
| 图表库 | ECharts 5 |
| Markdown 渲染 | markdown-it |
| HTTP 客户端 | Axios |

---

## 一、在 IntelliJ IDEA 中运行

### 1. 用 IDEA 打开项目

启动 IntelliJ IDEA → **File → Open** → 选择 `ATS` 文件夹 → **OK**。

IDEA 会自动识别根目录下的 `pom.xml`，并提示 "Maven build scripts found"，点击 **Load** 加载 Maven 依赖。

### 2. 配置 JDK

确保 IDEA 已配置 JDK 17+：
- **File → Project Structure → Project** → SDK 选择 JDK 17 或更高版本

### 3. 运行 Spring Boot 应用

打开 `src/main/java/com/hr/ats/AtsApplication.java`，点击 `main` 方法左侧的绿色三角按钮 ▶，选择 **Run 'AtsApplication'**。

或者使用 Maven 面板：右侧边栏 **Maven → ATS → Plugins → spring-boot → spring-boot:run**。

启动成功后控制台会显示：
```
Started AtsApplication in X.XXX seconds
```

### 4. 验证后端服务

浏览器访问：http://localhost:8080/webapi/positions

如果返回 JSON 数据（含 5 条示例岗位），说明后端启动成功。

### 5. H2 数据库控制台（可选）

浏览器访问：http://localhost:8080/webapi/h2-console

| 字段 | 值 |
|------|-----|
| JDBC URL | `jdbc:h2:mem:atsdb` |
| 用户名 | `sa` |
| 密码 | 留空 |

点击 **Connect** 即可查看 `T_POSITION` 表数据。

---

## 二、启动前端服务

**前置要求**：Node.js 18+

```bash
cd ats-frontend
npm install
npm run dev
```

启动后访问：http://localhost:3000

> 前端开发服务器已配置代理，所有 `/webapi` 请求会自动转发到后端 `http://localhost:8080`。

---

## 三、API 接口列表

基础路径：`/webapi`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/positions` | 分页查询岗位列表（支持 keyword、status 筛选） |
| GET | `/positions/all` | 获取所有岗位（不分页） |
| GET | `/positions/stats` | 岗位统计（状态分布 + 月度趋势） |
| GET | `/positions/{id}` | 获取单个岗位详情 |
| POST | `/positions` | 新增岗位 |
| PUT | `/positions/{id}` | 更新岗位信息 |
| DELETE | `/positions/{id}` | 删除岗位 |
| POST | `/positions/upload` | Excel 批量导入岗位 |
| GET | `/positions/template` | 下载 Excel 导入模板 |
| POST | `/positions/{id}/submit` | 提交审核（DRAFT → REVIEWING） |
| POST | `/positions/{id}/approve` | 审核通过（REVIEWING → PUBLISHED） |
| POST | `/positions/{id}/reject` | 审核驳回（REVIEWING → REJECTED） |
| POST | `/positions/auto-close` | 手动触发过期岗位自动关闭 |

### 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

### 岗位状态说明

| 状态值 | 含义 |
|--------|------|
| `DRAFT` | 草稿（新建岗位默认状态） |
| `REVIEWING` | 审核中 |
| `REJECTED` | 已驳回（可重新提交审核） |
| `PUBLISHED` | 已发布 |
| `CLOSED` | 已关闭（手动关闭或过期自动关闭） |

### 审核流程

```
DRAFT → submit → REVIEWING → approve → PUBLISHED
                              → reject  → REJECTED → submit → REVIEWING
```

---

## 四、从 H2 切换到 MySQL

MySQL 驱动和配置文件已就绪，执行以下步骤：

1. 创建数据库：`CREATE DATABASE atsdb DEFAULT CHARACTER SET utf8mb4;`
2. 修改 `src/main/resources/application-mysql.yml` 中的数据库连接信息（用户名、密码）
3. 启动时激活 MySQL 配置：

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

生产环境建议将 `schema.sql` 中 `MODE=MySQL` 移除，将 `TEXT` 改为 `LONGTEXT`。

---

## 五、项目结构

```
ATS/                              ← IDEA 打开的根目录
├── pom.xml                       ← Maven 配置（Spring Boot 父 POM）
├── mvnw / mvnw.cmd               ← Maven Wrapper
├── README.md
├── src/
│   ├── main/java/com/hr/ats/
│   │   ├── AtsApplication.java           ← Spring Boot 启动类
│   │   ├── common/                       ← 统一响应、全局异常处理
│   │   │   ├── Result.java
│   │   │   ├── ResultCode.java
│   │   │   ├── BusinessException.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── config/                       ← CORS、定时任务配置
│   │   │   ├── WebConfig.java
│   │   │   └── PositionScheduler.java
│   │   ├── controller/                   ← 控制器层
│   │   │   └── PositionController.java
│   │   ├── dto/                          ← 数据传输对象
│   │   │   ├── PositionDTO.java
│   │   │   └── PositionExcelDTO.java
│   │   ├── entity/                       ← 数据库实体
│   │   │   └── Position.java
│   │   ├── repository/                   ← 数据访问层
│   │   │   └── PositionRepository.java
│   │   └── service/                      ← 业务逻辑层
│   │       ├── PositionService.java
│   │       └── impl/
│   │           ├── PositionServiceImpl.java
│   │           └── PositionExcelListener.java
│   └── main/resources/
│       ├── application.yml               ← 应用配置（H2）
│       ├── application-mysql.yml         ← MySQL 环境配置
│       ├── schema.sql                    ← 建表 DDL
│       └── data.sql                      ← 示例数据（5条，覆盖全部状态）
├── .mvn/
│   └── jvm.config                       ← JVM 参数（UTF-8 编码）
└── ats-frontend/                         ← 前端 Vue 3 项目
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── api/                          ← API 请求层
        │   ├── request.js
        │   └── position.js
        ├── styles/                       ← 全局样式
        │   └── design-system.css
        ├── components/                   ← 可复用组件
        │   ├── PositionTable.vue
        │   └── FileUpload.vue
        ├── router/                       ← 路由配置
        │   └── index.js
        └── views/                        ← 页面视图
            ├── PositionList.vue
            ├── PositionForm.vue
            ├── PositionDetail.vue
            └── Dashboard.vue
```
