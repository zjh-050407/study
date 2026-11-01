-- Batch E incremental DDL
-- Run with: mysql -uroot -p student_manager < increment_batchE.sql
-- 注意：库中 semester 表与 student.password 列已存在（历史演进），本脚本保持幂等：
--   1) semester 表按库内实际结构 CREATE IF NOT EXISTS
--   2) student 表仅补充 status 列（password 已存在，不重复添加）

CREATE TABLE IF NOT EXISTS `semester` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '学期名称',
  `start_date` DATE NULL,
  `end_date` DATE NULL,
  `is_current` TINYINT NOT NULL DEFAULT 0 COMMENT '1=当前学期',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_semester_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `student`
  ADD COLUMN `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1=正常 0=禁用' AFTER `password`;