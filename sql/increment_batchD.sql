-- Batch D incremental DDL
-- Run with: mysql -uroot -p student_manager < increment_batchD.sql
--
-- NOTE: `notice` table already existed in student_manager (created outside this repo's
-- student_manager.sql) with columns: id/title/content/type/target_role/is_top/status/
-- publisher/publish_time/create_time/update_time. Backend Notice mapper is adapted to
-- that existing schema, so no DDL change is needed for notice.
--
-- `course_selection` is newly created by this script.

CREATE TABLE IF NOT EXISTS `course_selection` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `sid` INT NOT NULL,
  `course_id` INT NOT NULL,
  `uid` INT NULL,
  `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sid_course` (`sid`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;