-- 批次J：对标补强 阶段一（教室管理 + 教学任务 + 分页索引）
-- 1. 教室/场地表
CREATE TABLE IF NOT EXISTS classroom (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL UNIQUE,
  capacity INT NOT NULL DEFAULT 60,
  type VARCHAR(20) NOT NULL DEFAULT '普通教室',
  building VARCHAR(50),
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '教室/场地';

-- 2. 教学任务表（培养方案→开课流水线）
CREATE TABLE IF NOT EXISTS teach_task (
  id INT PRIMARY KEY AUTO_INCREMENT,
  course_id INT NOT NULL,
  semester_id INT NULL,
  cid INT NOT NULL,
  tid INT NULL,
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0=未排课 1=已排课',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '教学任务';

-- 3. 索引说明：course_selection.uk_sid_course 已存在；score 已有 fk_score_student/idx_score_sid_semester；
--    exam_arrange 已有 idx_exam_course/idx_exam_semester；evaluation_record 已有 uk_task_course_teacher_token。
--    以上索引已覆盖分页与防重需求，无需重复创建。
