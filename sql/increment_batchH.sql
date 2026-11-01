-- Batch H incremental DDL（幂等，可重复执行）
-- Run with: mysql -uroot -p student_manager < increment_batchH.sql
-- 1) exam_arrange    考试安排（课程/学期/日期/节次/考场/监考教师/考试类型）
-- 2) graduate_audit  毕业审核结论留档（含缺项明细 JSON）

CREATE TABLE IF NOT EXISTS `exam_arrange` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `course_id` INT NOT NULL COMMENT '课程ID',
  `semester_id` INT NULL COMMENT '学期ID（NULL=未分学期）',
  `exam_date` DATE NOT NULL COMMENT '考试日期',
  `start_section` TINYINT NOT NULL COMMENT '开始节次（1-12）',
  `end_section` TINYINT NOT NULL COMMENT '结束节次（1-12，>=开始节次）',
  `room` VARCHAR(100) NOT NULL COMMENT '考场',
  `invigilator_uid` INT NULL COMMENT '监考教师 teacher.tid',
  `type` VARCHAR(20) NOT NULL DEFAULT '期末' COMMENT '考试类型：期末/补考/重修',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_exam_semester` (`semester_id`),
  KEY `idx_exam_course` (`course_id`),
  KEY `idx_exam_room_date` (`room`, `exam_date`),
  KEY `idx_exam_invigilator_date` (`invigilator_uid`, `exam_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `graduate_audit` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `sid` INT NOT NULL COMMENT '学生ID',
  `semester_id` INT NULL COMMENT '审核所属学期（默认当前学期，NULL=未分学期）',
  `conclusion` VARCHAR(20) NOT NULL COMMENT '结论：通过/不通过',
  `detail_json` TEXT COMMENT '审核明细 JSON（学分/绩点/必修/缺项明细）',
  `auditor_uid` INT NULL COMMENT '审核人 admin.uid',
  `audit_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_audit_sid` (`sid`),
  KEY `idx_audit_semester` (`semester_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;