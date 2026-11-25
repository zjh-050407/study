-- 批次O：选课轮次（初选志愿投档 + 补退选先到先得）（幂等，可重复执行）
-- 1. 选课轮次
CREATE TABLE IF NOT EXISTS selection_round (
  id INT AUTO_INCREMENT PRIMARY KEY,
  semester_id INT NOT NULL DEFAULT 0 COMMENT '学期（0=未分学期哨兵）',
  name VARCHAR(100) NOT NULL COMMENT '轮次名称（如 2026-2027-1 初选）',
  type TINYINT NOT NULL DEFAULT 1 COMMENT '1初选(志愿投档)/2补退选(先到先得)',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0未开始/1进行中/2已结束',
  start_time DATETIME,
  end_time DATETIME,
  max_wishes INT NOT NULL DEFAULT 3 COMMENT '初选每人志愿上限',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_round_sem (semester_id, type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课轮次';

-- 2. 学生选课志愿（初选轮次）
CREATE TABLE IF NOT EXISTS selection_wish (
  id INT AUTO_INCREMENT PRIMARY KEY,
  round_id INT NOT NULL,
  sid INT NOT NULL,
  course_id INT NOT NULL,
  priority INT NOT NULL DEFAULT 1 COMMENT '志愿序号 1..N',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_round_sid_course (round_id, sid, course_id),
  KEY idx_wish_round (round_id, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生选课志愿';
