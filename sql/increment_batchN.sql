-- 批次N：成绩流程闭环 + 证明打印（幂等，可重复执行）
-- 1. score 表加状态列（存量数据视为已发布）
SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'score' AND COLUMN_NAME = 'status');
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE score ADD COLUMN status TINYINT NOT NULL DEFAULT 2 COMMENT ''0草稿/1待审核/2已发布/3复核中''',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists := (SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'score' AND INDEX_NAME = 'idx_score_status');
SET @ddl := IF(@idx_exists = 0,
  'ALTER TABLE score ADD INDEX idx_score_status (status)',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2. 成绩复核申诉表
CREATE TABLE IF NOT EXISTS score_appeal (
  id INT AUTO_INCREMENT PRIMARY KEY,
  score_id INT NOT NULL,
  sid INT NOT NULL,
  reason VARCHAR(500) NOT NULL,
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0待处理/1通过/2驳回',
  new_total DECIMAL(5,1) COMMENT '复核通过后的新总评（可选）',
  handle_note VARCHAR(500) COMMENT '处理意见',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  handle_time DATETIME,
  handle_uid BIGINT,
  KEY idx_appeal_status (status),
  KEY idx_appeal_sid (sid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩复核申诉';
