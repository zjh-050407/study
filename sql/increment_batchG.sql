-- Batch G incremental DDL（幂等，可重复执行）
-- Run with: mysql -uroot -p student_manager < increment_batchG.sql
-- 1) evaluation_task   评教任务（按学期发起，起止时间，状态）
-- 2) evaluation_record 评教记录（匿名，不存学生 id，以 SHA-256 token 防重）

CREATE TABLE IF NOT EXISTS `evaluation_task` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `semester_id` INT NOT NULL COMMENT '学期ID（发起时锁定，默认当前学期）',
  `start_time` DATETIME NOT NULL COMMENT '评教开始时间',
  `end_time` DATETIME NOT NULL COMMENT '评教结束时间',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0=已关闭 1=进行中',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_task_semester` (`semester_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `evaluation_record` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `task_id` INT NOT NULL COMMENT '评教任务ID',
  `course_id` INT NOT NULL COMMENT '课程ID',
  `teacher_uid` INT NOT NULL COMMENT '被评教师ID（course_arrange.tid）',
  `stu_token` CHAR(64) NOT NULL COMMENT '学生匿名令牌 SHA-256（不存学号，防重复评）',
  `dim1` TINYINT NOT NULL COMMENT '教学态度 1-5 分',
  `dim2` TINYINT NOT NULL COMMENT '教学内容 1-5 分',
  `dim3` TINYINT NOT NULL COMMENT '教学方法 1-5 分',
  `dim4` TINYINT NOT NULL COMMENT '教学效果 1-5 分',
  `suggestion` VARCHAR(500) NULL COMMENT '文字建议（匿名）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_course_teacher_token` (`task_id`, `course_id`, `teacher_uid`, `stu_token`),
  KEY `idx_record_task` (`task_id`),
  KEY `idx_record_course_teacher` (`course_id`, `teacher_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;