-- Batch F incremental DDL（幂等，可重复执行）
-- Run with: mysql -uroot -p student_manager < increment_batchF.sql
-- 1) course_arrange / score / course_selection 挂学期（NULL=未分学期，兼容旧数据）
-- 2) 新建退选审批表 selection_apply

SET @dbname = DATABASE();

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'course_arrange' AND COLUMN_NAME = 'semester_id') = 0,
              'ALTER TABLE `course_arrange` ADD COLUMN `semester_id` INT NULL COMMENT ''学期ID，NULL=未分学期'' AFTER `course_id`',
              'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'score' AND COLUMN_NAME = 'semester_id') = 0,
              'ALTER TABLE `score` ADD COLUMN `semester_id` INT NULL COMMENT ''学期ID，NULL=未分学期'' AFTER `course_id`',
              'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'course_selection' AND COLUMN_NAME = 'semester_id') = 0,
              'ALTER TABLE `course_selection` ADD COLUMN `semester_id` INT NULL COMMENT ''学期ID，NULL=未分学期'' AFTER `course_id`',
              'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `selection_apply` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `sid` INT NOT NULL COMMENT '学生ID',
  `course_id` INT NOT NULL COMMENT '课程ID',
  `reason` VARCHAR(500) NULL COMMENT '退选原因',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=待审批 1=已通过 2=已拒绝',
  `apply_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `approve_uid` INT NULL COMMENT '审批人',
  `approve_time` DATETIME NULL,
  PRIMARY KEY (`id`),
  KEY `idx_apply_sid` (`sid`),
  KEY `idx_apply_course` (`course_id`),
  KEY `idx_apply_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;