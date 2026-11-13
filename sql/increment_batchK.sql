-- 批次K：阶段二 选课容量名额表（DB 降级模式与 Redis 配额同构；semester_id=0 表示未分学期，与项目学期哨兵一致）
CREATE TABLE IF NOT EXISTS selection_quota (
  course_id INT NOT NULL,
  semester_id INT NOT NULL DEFAULT 0,
  capacity INT NOT NULL DEFAULT 50,
  used INT NOT NULL DEFAULT 0,
  PRIMARY KEY (course_id, semester_id)
) COMMENT '选课容量名额（降级模式）';
