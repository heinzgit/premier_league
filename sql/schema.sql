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