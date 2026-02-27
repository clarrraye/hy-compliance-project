# hy-compliance-project
海渔合规管理平台

## 🚀 快速启动指南

### 1. 极速启动 (推荐: Docker Compose)
无需本地安装 MySQL，只需安装 Docker Desktop。

```bash
# 一键启动数据库 (自动导入数据，端口映射为 3307)
docker-compose up -d hy-db
```

### 2. 后端启动 (Spring Boot)
后端配置已默认指向 Docker 数据库 (端口 3307，密码 hy_root_password)。
直接运行即可，无需修改任何配置。

```bash
cd hy-backend
.\mvnw spring-boot:run
```

### 3. 前端启动 (Vue 3)
前端运行在 `8080` 端口，已配置代理转发到后端。

```bash
cd hy-frontend
npm install   # 仅首次运行或依赖变更时需要
npm run serve
```

### 4. 数据库管理 (如何修改数据)
由于数据库运行在 Docker 容器中 (端口 `3307`)，你可以使用任何数据库管理工具连接：

- **工具推荐**: DBeaver, Navicat, DataGrip 或 VS Code Database 插件
- **连接配置**:
  - **Host**: `localhost`
  - **Port**: `3307`
  - **Database**: `hy_compliance_db`
  - **Username**: `root`
  - **Password**: `hy_root_password`

> **提示**: 如果修改了表结构想同步给团队，请导出 SQL 脚本覆盖根目录的 `_localhost-...dump.sql` 文件。

### 5. 常见问题


