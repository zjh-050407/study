-- 批次M：消息通知+待办、校历（幂等，可重复执行）
-- 1. 个人消息表
CREATE TABLE IF NOT EXISTS message (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  receiver_type VARCHAR(20) NOT NULL COMMENT 'admin/teacher/student',
  receiver_id BIGINT NOT NULL COMMENT '接收人ID（uid或sid）',
  type VARCHAR(20) NOT NULL DEFAULT 'system' COMMENT 'system/approval/notice/selection',
  title VARCHAR(200) NOT NULL,
  content VARCHAR(1000),
  biz_id BIGINT COMMENT '关联业务ID',
  is_read TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_receiver (receiver_type, receiver_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息';

-- 2. 公告已读游标（公告不进 message 表，按最后已读ID计算未读数）
CREATE TABLE IF NOT EXISTS user_notice_read (
  user_key VARCHAR(64) PRIMARY KEY COMMENT '如 admin:1001 / student:1',
  last_notice_id BIGINT NOT NULL DEFAULT 0,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户公告已读游标';

-- 3. 教学周历（校历）
CREATE TABLE IF NOT EXISTS teaching_week (
  id INT AUTO_INCREMENT PRIMARY KEY,
  semester_id INT NOT NULL DEFAULT 0 COMMENT '0=未分学期哨兵',
  week_no INT NOT NULL COMMENT '第几教学周',
  start_date DATE,
  end_date DATE,
  note VARCHAR(200) COMMENT '备注（如 考试周/放假）',
  UNIQUE KEY uk_sem_week (semester_id, week_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学周历';

-- 4. 教师账号与教师档案关联（演示数据：bhl/zhangsan 补档案，幂等）
INSERT IGNORE INTO teacher (username, password, name, age, title, department, employee_no, did, status)
SELECT 'bhl', password, '白慧兰', 45, '教授', 'AI工程师学院', 'T1001', 1, 1 FROM admin WHERE username = 'bhl';
INSERT IGNORE INTO teacher (username, password, name, age, title, department, employee_no, did, status)
SELECT 'zhangsan', password, '张三', 38, '讲师', 'AI工程师学院', 'T1002', 1, 1 FROM admin WHERE username = 'zhangsan';
