-- Batch I incremental DDL（幂等，可重复执行）
-- Run with: mysql -uroot -p student_manager < increment_batchI.sql
-- 1) training_plan / plan_course  培养方案（按班级）+ 方案课程（必修/限选/任选 + 学分要求）
-- 2) status_change               学籍异动（休学/复学/转专业/退学 + 生效日期 + 经办人）

CREATE TABLE IF NOT EXISTS `training_plan` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `cid` INT NOT NULL COMMENT '适用班级 clazz.cid（专业按班级维度落地）',
  `name` VARCHAR(100) NULL COMMENT '方案名称',
  `total_credit_required` DECIMAL(5,1) NOT NULL DEFAULT 0 COMMENT '总学分要求',
  `required_credit_required` DECIMAL(5,1) NOT NULL DEFAULT 0 COMMENT '必修学分要求',
  `elective_credit_required` DECIMAL(5,1) NOT NULL DEFAULT 0 COMMENT '选修（限选+任选）学分要求',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plan_cid` (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `plan_course` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `plan_id` INT NOT NULL COMMENT '培养方案ID',
  `course_id` INT NOT NULL COMMENT '课程ID',
  `type` VARCHAR(10) NOT NULL COMMENT '课程类型：必修/限选/任选',
  `require_credit` DECIMAL(5,1) NOT NULL DEFAULT 0 COMMENT '该课程学分要求',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plan_course` (`plan_id`, `course_id`),
  KEY `idx_plan_course_plan` (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `status_change` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `sid` INT NOT NULL COMMENT '学生ID',
  `type` VARCHAR(10) NOT NULL COMMENT '异动类型：休学/复学/转专业/退学',
  `effective_date` DATE NOT NULL COMMENT '生效日期',
  `reason` VARCHAR(500) NULL COMMENT '原因说明',
  `target_cid` INT NULL COMMENT '转专业目标班级 clazz.cid',
  `operator_uid` INT NULL COMMENT '经办人 admin.uid',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_sid` (`sid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;