# 素材制作网站

## 当前状态

项目当前完成 P0-P6：工程骨架、数据/后端基础、模板上传、异步任务、前端业务闭环、关键测试和容器/CI 交付。本地开发使用 MySQL，Docker Compose 作为评估环境的一键交付方式。

- `backend/`：Spring Boot 3.4.5、Java 17、MyBatis 3.0.4
- `frontend/`：Next.js 15.3.2、React 19.1.0、TypeScript 5.8.3、Node 22
- 根目录 Maven reactor 已纳入后端模块
- `.env.example` 提供本地配置示例
- `backend/src/main/resources/db/`：MySQL 表结构和演示初始化数据
- `frontend/app/templates/`：模板列表、详情上传和任务进度
- `frontend/app/history/`：历史任务和刷新恢复后的轮询
- `docker-compose.yml`：MySQL、Java 后端、Next.js 前端一键启动
- `.github/workflows/ci.yml`：push 和 pull request 的前后端检查

## 项目结构

```text
backend/
  src/main/java/                 Java 接口、服务、异步任务和 MyBatis mapper
  src/main/resources/db/         MySQL 表结构和种子数据
  Dockerfile
frontend/
  app/templates/                 模板列表、详情和上传
  app/history/                   历史任务
  lib/                           API 客户端和任务状态逻辑
  Dockerfile
docker-compose.yml
.github/workflows/ci.yml
```

## 本地启动

本地开发不依赖 Docker。请先安装并启动 MySQL 8.0+，项目默认连接：

```text
地址：localhost:3306
数据库：material_maker
用户名：material_maker
密码：material_maker
```

Windows 安装 MySQL 后可在管理员 PowerShell 启动服务：

```powershell
Start-Service MySQL80
Test-NetConnection localhost -Port 3306
```

如果数据库账号还不存在，使用 root 登录 MySQL 后执行：

```sql
CREATE DATABASE IF NOT EXISTS material_maker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'material_maker'@'localhost' IDENTIFIED BY 'material_maker';
GRANT ALL PRIVILEGES ON material_maker.* TO 'material_maker'@'localhost';
FLUSH PRIVILEGES;
```

后端：

```bash
mvn -f backend/pom.xml spring-boot:run
```

后端启动时会自动执行 `schema.sql` 和 `data.sql`。初始化脚本支持重复执行，适合本地开发；生产环境建议将 `DB_INIT_MODE` 设置为 `never` 并使用正式迁移工具。

前端：

```bash
cd frontend
npm install
npm run dev
```

前端质量检查：

```bash
npm run test
npm run lint
npm run typecheck
npm run build
```

后端测试：

```bash
mvn test
```

后端健康检查地址：`http://localhost:8080/api/health`

## Docker Compose 启动

需要 Docker Desktop 或兼容 Docker Compose 的环境：

```bash
docker compose up --build
```

启动后访问：

- 前端：`http://localhost:3000`
- 后端健康检查：`http://localhost:8080/api/health`
- MySQL：`localhost:3306`，数据库 `material_maker`

停止服务：

```bash
docker compose down
```

同时删除数据库和上传文件卷（仅适用于重新开始本地演示）：

```bash
docker compose down -v
```

## 数据库配置

默认连接 `localhost:3306/material_maker`，用户名和密码均为 `material_maker`。可通过 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`DB_INIT_MODE` 和 `UPLOAD_DIR` 覆盖。开发环境默认执行 `schema.sql` 和 `data.sql`，且初始化脚本支持重复执行；生产配置应将 `DB_INIT_MODE` 调整为 `never`，并使用独立迁移流程。

当前初始化数据包含一个演示用户和三个不同素材数量要求的模板。任务、素材和步骤表已初始化，模板、上传、任务和历史接口均已接入。

## 模板和素材接口

- `GET /api/templates?category=创意图片`：读取启用模板并支持分类筛选
- `GET /api/templates/{id}`：读取模板详情
- `POST /api/assets?templateId=demo-postcard`：以 `multipart/form-data` 的 `file` 字段上传 JPG/PNG；可用 `X-Demo-User` 指定演示用户，默认 `demo-user`
- 上传成功后，服务端将文件写入 `UPLOAD_DIR`，返回 `/uploads/{filename}` 预览地址，并将素材元数据保存到 MySQL
- 服务端会同时执行通用校验和模板级校验：空文件、无效图片内容、非 JPG/PNG、模板不支持的 MIME、超过模板大小、未知演示用户和服务端保存失败均返回统一错误结构

## P3 异步任务接口

- `POST /api/tasks`：提交 `{ "templateId": "demo-postcard", "assetIds": [1, 2], "stepDelayMs": 1400 }`，创建后立即返回任务 ID
- `GET /api/tasks/{id}`：只读查询当前阶段、三步状态、失败原因和结果地址
- `GET /api/tasks`：读取当前演示用户的历史任务
- `failStage` 可设为 `ASSET_CHECK`、`GENERATION` 或 `RESULT_ASSEMBLY`，用于稳定演示指定阶段失败；`stepDelayMs` 可调整每步模拟耗时，最大 60000 毫秒
- Java 后台使用受控线程池顺序执行三步，任务状态持久化到 MySQL，查询不会推动任务执行

### 任务状态和阶段

- 整体状态：`WAITING`、`PROCESSING`、`SUCCEEDED`、`FAILED`
- 步骤：`ASSET_CHECK`、`GENERATION`、`RESULT_ASSEMBLY`
- 失败任务保存失败阶段和原因，失败后不会执行后续步骤
- 任务受理接口只创建记录并返回 ID，后台线程池在事务提交后开始执行

## 验收演示

1. 启动 Compose，进入模板列表并按分类筛选。
2. 打开“旅行明信片”，上传两张 JPG/PNG 图片。
3. 提交正常任务，观察三步状态从等待、处理中到完成，并查看结果预览。
4. 在失败阶段下拉框选择“模拟生成失败”，重新创建任务，确认后续“结果整理”不会执行。
5. 将多个任务连续提交，确认各任务的状态和结果互不影响。
6. 在任务处理中刷新“历史记录”，确认记录从 Java 接口恢复并继续轮询。
7. 使用 `stepDelayMs` 调整接口请求中的模拟耗时，便于观察处理中状态。

## CI 和测试

本地检查：

```bash
cd frontend
npm ci
npm run lint
npm run test
npm run typecheck
npm run build
cd ..
mvn test
mvn -DskipTests package
```

GitHub Actions 在 push 和 pull request 时执行同样的前端测试/类型检查/构建，以及 Java 测试和打包。

## 身份、限制和边界

当前使用 `X-Demo-User` 作为演示身份，默认值为 `demo-user`，只用于隔离本题中的个人素材和任务，不是生产认证方案。系统不实现真实 AI、支付、第三方账号、复杂模板后台、自动重试、幂等受理、断点续跑或服务重启后的任务恢复。结果图片由本地 SVG 模拟，不调用外部 AI 服务。

应用运行期间的上传文件写入 Docker volume；数据库初始化脚本适合开发/验收环境，生产环境应设置 `DB_INIT_MODE=never` 并使用迁移工具。当前 Compose 使用本地演示密码，不得用于生产环境。

## 部署和回滚

部署时使用固定 commit 构建镜像并执行 `docker compose up --build -d`。回滚时切换到上一个验收 commit，重新构建并启动；数据库表结构没有迁移版本控制时，不要直接删除生产数据卷，先确认兼容性。开发环境如需完全重置，可使用 `docker compose down -v` 后重新启动。

## AI 和第三方来源说明

本项目使用 AI 编程工具辅助拆分需求、生成初稿、修改代码和测试，并通过 Maven、Node 测试、ESLint、TypeScript 检查、Next.js 构建和人工审阅进行验证与修正。未使用真实账号、私密凭据或私人聊天记录。模板字段、演示用户、SVG 结果图和模拟任务流程均为本项目自构造内容；未复用公司内部代码或付费素材。

## 当前验收状态

P0-P6 的代码和交付材料已完成。本地 MySQL 启动、数据库初始化、后端健康接口、模板接口和示例封面资源已验证；后端 Maven 测试/打包、前端测试/lint/类型检查/生产构建均已通过。Docker Compose 配置已通过 `docker compose config` 校验，完整容器启动需要评估环境的 Docker Linux Engine 正常运行。
