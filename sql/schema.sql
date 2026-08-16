-- Premier League 数据录入系统 - 数据库 Schema
-- 在 MySQL Workbench 中手动执行此脚本

CREATE DATABASE IF NOT EXISTS premier_league
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE premier_league;

-- 球队表
CREATE TABLE IF NOT EXISTS teams (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  name         VARCHAR(100) NOT NULL UNIQUE,
  short_name   VARCHAR(20),
  founded_year INT,
  stadium      VARCHAR(100),
  city         VARCHAR(50),
  created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 比赛表
CREATE TABLE IF NOT EXISTS matches (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  home_team_id  BIGINT NOT NULL,
  away_team_id  BIGINT NOT NULL,
  home_score    INT    NOT NULL DEFAULT 0,
  away_score    INT    NOT NULL DEFAULT 0,
  match_date    DATE   NOT NULL,
  season        VARCHAR(20) NOT NULL,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_match_home FOREIGN KEY (home_team_id) REFERENCES teams(id),
  CONSTRAINT fk_match_away FOREIGN KEY (away_team_id) REFERENCES teams(id),
  INDEX idx_matches_season (season),
  INDEX idx_matches_home   (home_team_id),
  INDEX idx_matches_away   (away_team_id),
  INDEX idx_matches_date   (match_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 进球表
CREATE TABLE IF NOT EXISTS goals (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  match_id    BIGINT NOT NULL,
  team_id     BIGINT NOT NULL,
  scorer_name VARCHAR(100) NOT NULL,
  minute      INT,
  goal_type   VARCHAR(20) DEFAULT 'REGULAR',  -- REGULAR / PENALTY / OWN_GOAL
  CONSTRAINT fk_goal_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE,
  CONSTRAINT fk_goal_team  FOREIGN KEY (team_id)  REFERENCES teams(id),
  INDEX idx_goals_scorer (scorer_name),
  INDEX idx_goals_match  (match_id),
  INDEX idx_goals_team   (team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 阵容表:每场比赛每队一条 (lineup per match per team)
CREATE TABLE IF NOT EXISTS lineups (
  id        BIGINT AUTO_INCREMENT PRIMARY KEY,
  match_id  BIGINT NOT NULL,
  team_id   BIGINT NOT NULL,
  formation VARCHAR(20),                  -- 例如 "4-3-3"
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_lineup_match_team UNIQUE (match_id, team_id),
  CONSTRAINT fk_lineup_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE,
  CONSTRAINT fk_lineup_team  FOREIGN KEY (team_id)  REFERENCES teams(id),
  INDEX idx_lineup_match (match_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 阵容球员
CREATE TABLE IF NOT EXISTS lineup_players (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  lineup_id     BIGINT NOT NULL,
  player_name   VARCHAR(100) NOT NULL,
  position      VARCHAR(20),                -- 例如 GK/DF/MF/FW, 也可更细 LB/CB/RB 等
  display_order INT NOT NULL DEFAULT 0,    -- 1..11,用于展示顺序
  pos_x         INT,                       -- 自定义阵型时球场坐标 (0-100 百分比), NULL 用预设阵型默认
  pos_y         INT,
  shirt_number  VARCHAR(10),               -- 球衣号码, 用户自由输入
  CONSTRAINT fk_lp_lineup FOREIGN KEY (lineup_id) REFERENCES lineups(id) ON DELETE CASCADE,
  INDEX idx_lp_lineup (lineup_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 已有库需要执行 (幂等):
-- ALTER TABLE lineup_players ADD COLUMN pos_x INT NULL, ADD COLUMN pos_y INT NULL;
-- ALTER TABLE lineup_players ADD COLUMN shirt_number VARCHAR(10) NULL;