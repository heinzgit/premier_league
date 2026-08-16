# 英超数据录入系统

一个简单的 Spring Boot + 纯 HTML/JS Web 应用,用于录入英超比赛数据,自动计算积分榜、射手榜,并查询球队历史战绩。

## 技术栈

- **后端**: Spring Boot 3.3.5 + Spring Data JPA
- **数据库**: MySQL 8.x
- **前端**: 纯 HTML + 原生 JavaScript (由 Spring Boot 托管)

## 项目结构

```
Premier_league/
├── sql/schema.sql                    # 数据库脚本,在 Workbench 执行
└── backend/
    ├── pom.xml
    └── src/main/
        ├── java/com/example/premier/
        │   ├── PremierLeagueApplication.java
        │   ├── entity/   (Team, Match, Goal)
        │   ├── repository/
        │   ├── service/  (TeamService, MatchService, StandingService, ScorerService, TeamHistoryService)
        │   ├── controller/
        │   └── dto/
        └── resources/
            ├── application.properties
            └── static/   # 前端 (index/teams/matches/standings/scorers/team-history)
```

## 初始化步骤

### 1. 创建数据库

在 MySQL Workbench 中打开 `sql/schema.sql` 并执行,创建 `premier_league` 数据库和 `teams`、`matches`、`goals` 三张表。

### 2. 配置数据库密码

编辑 `backend/src/main/resources/application.properties`,把:

```
spring.datasource.password=YOUR_PASSWORD_HERE
```

改成你的 MySQL 密码。

### 3. 启动

```bash
cd backend
mvn spring-boot:run
```

第一次运行会下载依赖,稍等片刻。看到 `Started PremierLeagueApplication` 表示启动成功。

### 4. 访问

浏览器打开 <http://localhost:8080/>

## 使用流程

1. **球队管理** — 先录入若干球队
2. **比赛录入** — 录入比赛,可选填进球者(同一时刻可录入任意条)
3. **积分榜 / 射手榜** — 实时计算
4. **球队历史** — 选择球队查看其所有比赛及胜负统计

## API 端点

| Method | Path | 说明 |
|---|---|---|
| GET / POST | `/api/teams` | 列出 / 创建球队 |
| GET / PUT / DELETE | `/api/teams/{id}` | 详情 / 更新 / 删除 |
| GET | `/api/teams/{id}/history` | 该球队的所有比赛 |
| GET / POST | `/api/matches` | 列出 / 创建比赛 |
| GET / PUT / DELETE | `/api/matches/{id}` | 详情 / 更新 / 删除 |
| GET | `/api/standings?season=YYYY-YYYY` | 积分榜 |
| GET | `/api/scorers?season=YYYY-YYYY` | 射手榜 |

## 备注

- 球队若已参与比赛,不允许删除 (返回 409)
- 比赛删除时,其所有进球也会一并删除
- 进球类型: `REGULAR` (普通) / `PENALTY` (点球) / `OWN_GOAL` (乌龙球);射手榜默认排除乌龙球
- 赛季字段为字符串,如 `2025-2026`,留空表示全部赛季