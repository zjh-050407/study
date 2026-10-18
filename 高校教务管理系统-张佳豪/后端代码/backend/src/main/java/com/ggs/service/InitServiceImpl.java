package com.ggs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Service
public class InitServiceImpl implements InitService {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetDatabase() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        // Truncate business tables (keep admin)
        jdbcTemplate.execute("TRUNCATE TABLE score");
        jdbcTemplate.execute("TRUNCATE TABLE course_arrange");
        jdbcTemplate.execute("TRUNCATE TABLE course");
        jdbcTemplate.execute("TRUNCATE TABLE student");
        jdbcTemplate.execute("TRUNCATE TABLE clazz");
        jdbcTemplate.execute("TRUNCATE TABLE teacher");
        jdbcTemplate.execute("TRUNCATE TABLE department");

        // Insert initial departments
        jdbcTemplate.execute("INSERT INTO department (did, name, dean) VALUES (1, 'AI工程师学院', '王建国')");
        jdbcTemplate.execute("INSERT INTO department (did, name, dean) VALUES (2, '管理学院', '李明华')");
        jdbcTemplate.execute("INSERT INTO department (did, name, dean) VALUES (3, '商学院', '张伟')");
        jdbcTemplate.execute("INSERT INTO department (did, name, dean) VALUES (4, '食品学院', '陈秀英')");

        // Insert initial teachers
        jdbcTemplate.execute("INSERT INTO teacher (tid, name, age, title, department) VALUES (1, '王建国', 52, '教授', 'AI工程师学院')");
        jdbcTemplate.execute("INSERT INTO teacher (tid, name, age, title, department) VALUES (2, '李秀英', 45, '副教授', 'AI工程师学院')");
        jdbcTemplate.execute("INSERT INTO teacher (tid, name, age, title, department) VALUES (3, '张明德', 48, '教授', '管理学院')");
        jdbcTemplate.execute("INSERT INTO teacher (tid, name, age, title, department) VALUES (4, '刘慧敏', 39, '讲师', '管理学院')");
        jdbcTemplate.execute("INSERT INTO teacher (tid, name, age, title, department) VALUES (5, '陈志远', 55, '教授', '商学院')");
        jdbcTemplate.execute("INSERT INTO teacher (tid, name, age, title, department) VALUES (6, '周丽华', 42, '副教授', '商学院')");
        jdbcTemplate.execute("INSERT INTO teacher (tid, name, age, title, department) VALUES (7, '赵文轩', 50, '教授', '食品学院')");
        jdbcTemplate.execute("INSERT INTO teacher (tid, name, age, title, department) VALUES (8, '孙雅琴', 36, '讲师', '食品学院')");
        jdbcTemplate.execute("INSERT INTO teacher (tid, name, age, title, department) VALUES (9, '吴浩然', 44, '副教授', 'AI工程师学院')");
        jdbcTemplate.execute("INSERT INTO teacher (tid, name, age, title, department) VALUES (10, '郑思琪', 38, '讲师', '管理学院')");

        // Insert initial clazz
        jdbcTemplate.execute("INSERT INTO clazz (cid, name, did, tid, capacity) VALUES (1, '计科2301', 1, 1, 45)");
        jdbcTemplate.execute("INSERT INTO clazz (cid, name, did, tid, capacity) VALUES (2, '计科2302', 1, 2, 45)");
        jdbcTemplate.execute("INSERT INTO clazz (cid, name, did, tid, capacity) VALUES (3, '软工2301', 1, 9, 40)");
        jdbcTemplate.execute("INSERT INTO clazz (cid, name, did, tid, capacity) VALUES (4, '会计2301', 2, 3, 50)");
        jdbcTemplate.execute("INSERT INTO clazz (cid, name, did, tid, capacity) VALUES (5, '食品2301', 4, 7, 40)");

        // Insert initial students
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (1, '张伟', 20, '男', 1)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (2, '李娜', 19, '女', 1)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (3, '王强', 21, '男', 1)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (4, '刘洋', 20, '男', 1)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (5, '陈静', 19, '女', 1)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (6, '杨帆', 20, '男', 1)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (7, '赵敏', 19, '女', 2)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (8, '黄磊', 21, '男', 2)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (9, '周杰', 20, '男', 2)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (10, '吴倩', 19, '女', 2)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (11, '徐鹏', 20, '男', 2)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (12, '孙丽', 19, '女', 2)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (13, '马超', 21, '男', 3)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (14, '朱琳', 20, '女', 3)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (15, '胡军', 20, '男', 3)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (16, '郭芳', 19, '女', 3)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (17, '何伟', 21, '男', 4)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (18, '高燕', 20, '女', 4)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (19, '林涛', 20, '男', 4)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (20, '郑欣', 19, '女', 4)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (21, '谢峰', 21, '男', 4)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (22, '罗丹', 20, '女', 4)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (23, '梁辉', 20, '男', 5)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (24, '宋洁', 19, '女', 5)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (25, '唐勇', 21, '男', 5)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (26, '韩梅', 20, '女', 5)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (27, '曹阳', 20, '男', 5)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (28, '许蕾', 19, '女', 5)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (29, '冯磊', 21, '男', 5)");
        jdbcTemplate.execute("INSERT INTO student (sid, name, age, sex, cid) VALUES (30, '邓婷', 20, '女', 5)");

        // Insert initial courses
        jdbcTemplate.execute("INSERT INTO course (course_id, name, credit, hours) VALUES (1, '高等数学', 4.0, 64)");
        jdbcTemplate.execute("INSERT INTO course (course_id, name, credit, hours) VALUES (2, '大学英语', 3.0, 48)");
        jdbcTemplate.execute("INSERT INTO course (course_id, name, credit, hours) VALUES (3, '数据结构', 4.0, 64)");
        jdbcTemplate.execute("INSERT INTO course (course_id, name, credit, hours) VALUES (4, '操作系统', 3.5, 56)");
        jdbcTemplate.execute("INSERT INTO course (course_id, name, credit, hours) VALUES (5, '线性代数', 3.0, 48)");
        jdbcTemplate.execute("INSERT INTO course (course_id, name, credit, hours) VALUES (6, '概率论与数理统计', 3.0, 48)");
        jdbcTemplate.execute("INSERT INTO course (course_id, name, credit, hours) VALUES (7, '计算机网络', 3.5, 56)");
        jdbcTemplate.execute("INSERT INTO course (course_id, name, credit, hours) VALUES (8, '数据库原理', 3.5, 56)");
        jdbcTemplate.execute("INSERT INTO course (course_id, name, credit, hours) VALUES (9, '管理学原理', 3.0, 48)");
        jdbcTemplate.execute("INSERT INTO course (course_id, name, credit, hours) VALUES (10, '食品安全概论', 2.5, 40)");

        // Insert initial course_arrange
        jdbcTemplate.execute("INSERT INTO course_arrange (arrange_id, course_id, tid, classroom, week_day, section) VALUES (1, 1, 3, 'A101', 1, 1)");
        jdbcTemplate.execute("INSERT INTO course_arrange (arrange_id, course_id, tid, classroom, week_day, section) VALUES (2, 2, 4, 'B203', 1, 3)");
        jdbcTemplate.execute("INSERT INTO course_arrange (arrange_id, course_id, tid, classroom, week_day, section) VALUES (3, 3, 1, 'C305', 2, 1)");
        jdbcTemplate.execute("INSERT INTO course_arrange (arrange_id, course_id, tid, classroom, week_day, section) VALUES (4, 4, 2, 'A102', 2, 3)");
        jdbcTemplate.execute("INSERT INTO course_arrange (arrange_id, course_id, tid, classroom, week_day, section) VALUES (5, 5, 3, 'B204', 3, 1)");
        jdbcTemplate.execute("INSERT INTO course_arrange (arrange_id, course_id, tid, classroom, week_day, section) VALUES (6, 6, 5, 'A103', 3, 3)");
        jdbcTemplate.execute("INSERT INTO course_arrange (arrange_id, course_id, tid, classroom, week_day, section) VALUES (7, 7, 9, 'C306', 4, 1)");
        jdbcTemplate.execute("INSERT INTO course_arrange (arrange_id, course_id, tid, classroom, week_day, section) VALUES (8, 8, 1, 'B205', 4, 3)");
        jdbcTemplate.execute("INSERT INTO course_arrange (arrange_id, course_id, tid, classroom, week_day, section) VALUES (9, 9, 3, 'A104', 5, 1)");
        jdbcTemplate.execute("INSERT INTO course_arrange (arrange_id, course_id, tid, classroom, week_day, section) VALUES (10, 10, 7, 'B206', 5, 3)");

        // Insert initial scores
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (1, 1, 1, 85.0, 78.0, 82.0, 82.2)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (2, 1, 2, 90.0, 85.0, 88.0, 88.1)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (3, 1, 3, 88.0, 82.0, 85.0, 85.1)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (4, 2, 1, 92.0, 88.0, 90.0, 90.0)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (5, 2, 2, 85.0, 80.0, 83.0, 82.9)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (6, 2, 3, 78.0, 75.0, 80.0, 78.5)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (7, 3, 1, 80.0, 72.0, 75.0, 75.6)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (8, 3, 2, 88.0, 85.0, 86.0, 86.3)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (9, 3, 4, 82.0, 78.0, 80.0, 80.2)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (10, 4, 1, 95.0, 90.0, 92.0, 92.4)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (11, 4, 3, 90.0, 88.0, 89.0, 89.1)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (12, 4, 5, 85.0, 80.0, 82.0, 82.3)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (13, 5, 2, 92.0, 88.0, 90.0, 90.0)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (14, 5, 4, 78.0, 72.0, 75.0, 75.1)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (15, 5, 6, 88.0, 82.0, 85.0, 85.1)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (16, 6, 1, 75.0, 68.0, 70.0, 70.6)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (17, 6, 3, 82.0, 78.0, 80.0, 80.2)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (18, 6, 7, 90.0, 85.0, 88.0, 88.1)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (19, 7, 1, 88.0, 82.0, 85.0, 85.1)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (20, 7, 2, 90.0, 88.0, 89.0, 89.1)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (21, 7, 5, 82.0, 78.0, 80.0, 80.2)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (22, 8, 1, 70.0, 65.0, 68.0, 67.9)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (23, 8, 3, 85.0, 80.0, 82.0, 82.3)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (24, 8, 4, 90.0, 88.0, 89.0, 89.1)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (25, 9, 2, 78.0, 72.0, 75.0, 75.1)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (26, 9, 5, 88.0, 82.0, 85.0, 85.1)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (27, 9, 6, 92.0, 88.0, 90.0, 90.0)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (28, 10, 1, 85.0, 80.0, 82.0, 82.3)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (29, 10, 4, 90.0, 85.0, 88.0, 88.1)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (30, 10, 7, 82.0, 78.0, 80.0, 80.2)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (31, 11, 2, 88.0, 82.0, 85.0, 85.1)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (32, 11, 3, 75.0, 70.0, 72.0, 72.1)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (33, 11, 8, 90.0, 85.0, 88.0, 88.1)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (34, 12, 1, 92.0, 88.0, 90.0, 90.0)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (35, 12, 5, 85.0, 80.0, 82.0, 82.3)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (36, 12, 6, 78.0, 72.0, 75.0, 75.1)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (37, 13, 3, 88.0, 82.0, 85.0, 85.1)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (38, 13, 7, 90.0, 88.0, 89.0, 89.1)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (39, 13, 8, 82.0, 78.0, 80.0, 80.2)");
        jdbcTemplate.execute("INSERT INTO score (score_id, sid, course_id, usual, mid, final, total) VALUES (40, 14, 2, 85.0, 80.0, 82.0, 82.3)");

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }
}
