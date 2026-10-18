/*
 Navicat Premium Dump SQL

 Source Server         : root
 Source Server Type    : MySQL
 Source Server Version : 80406 (8.4.6)
 Source Host           : localhost:3306
 Source Schema         : student_manager

 Target Server Type    : MySQL
 Target Server Version : 80406 (8.4.6)
 File Encoding         : 65001

 Date: 18/06/2026 22:05:33
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `uid` int NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'admin',
  PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1007 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1001, 'admin', '$2a$10$Qd7r.DffoUZZiuyREu5HjO8MMJD2D5k81HebMKZVHmINmR.WKpuUC', NULL, 'admin@edu.com', 'admin');
INSERT INTO `admin` VALUES (1002, 'bhl', '123456', NULL, 'bhl@edu.com', 'professor');
INSERT INTO `admin` VALUES (1003, 'zhangsan', '123456', NULL, 'zhangsan@edu.com', 'lecturer');
INSERT INTO `admin` VALUES (1004, '左飞', '$2a$10$kx4idrxiYtsZlf20wuD38.bITfDnsk.VG5Q2Lr8FIcb1YqO7ecVpu', NULL, NULL, 'professor');
INSERT INTO `admin` VALUES (1005, 'guest', '$2a$10$xmBKZ3tOj0h0sooHUx159Of0YOsV1gPr/2C3uEHbk7khtn2hz5ebq', NULL, NULL, 'lecturer');
INSERT INTO `admin` VALUES (1006, '123', '$2a$10$2zJPOGxV9ynTtxbNmXyOCuEdnReKtzahWbgj/LocpPT6L4c8VvJPu', NULL, NULL, 'professor');

-- ----------------------------
-- Table structure for clazz
-- ----------------------------
DROP TABLE IF EXISTS `clazz`;
CREATE TABLE `clazz`  (
  `cid` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `did` int NULL DEFAULT NULL,
  `tid` int NULL DEFAULT NULL,
  `capacity` int NULL DEFAULT 50,
  PRIMARY KEY (`cid`) USING BTREE,
  INDEX `fk_clazz_dept`(`did` ASC) USING BTREE,
  INDEX `fk_clazz_teacher`(`tid` ASC) USING BTREE,
  CONSTRAINT `fk_clazz_dept` FOREIGN KEY (`did`) REFERENCES `department` (`did`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_clazz_teacher` FOREIGN KEY (`tid`) REFERENCES `teacher` (`tid`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of clazz
-- ----------------------------
INSERT INTO `clazz` VALUES (1, '计科2301', 1, 1, 45);
INSERT INTO `clazz` VALUES (2, '计科2302', 1, 9, 45);
INSERT INTO `clazz` VALUES (3, '软工2301', 1, 2, 40);
INSERT INTO `clazz` VALUES (4, '会计2301', 2, 11, 18);
INSERT INTO `clazz` VALUES (5, '食品2301', 4, 7, 40);

-- ----------------------------
-- Table structure for course
-- ----------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course`  (
  `course_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `credit` decimal(3, 1) NULL DEFAULT NULL,
  `hours` int NULL DEFAULT NULL,
  PRIMARY KEY (`course_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course
-- ----------------------------
INSERT INTO `course` VALUES (1, '高等数学', 4.0, 64);
INSERT INTO `course` VALUES (2, '大学英语', 3.0, 48);
INSERT INTO `course` VALUES (3, '数据结构', 4.0, 64);
INSERT INTO `course` VALUES (4, '操作系统', 3.5, 56);
INSERT INTO `course` VALUES (5, '线性代数', 3.0, 48);
INSERT INTO `course` VALUES (6, '概率论与数理统计', 3.0, 48);
INSERT INTO `course` VALUES (7, '计算机网络', 3.5, 56);
INSERT INTO `course` VALUES (8, '数据库原理', 3.5, 56);
INSERT INTO `course` VALUES (9, '管理学原理', 3.0, 48);
INSERT INTO `course` VALUES (10, '食品安全概论', 2.5, 40);

-- ----------------------------
-- Table structure for course_arrange
-- ----------------------------
DROP TABLE IF EXISTS `course_arrange`;
CREATE TABLE `course_arrange`  (
  `arrange_id` int NOT NULL AUTO_INCREMENT,
  `course_id` int NULL DEFAULT NULL,
  `tid` int NULL DEFAULT NULL,
  `classroom` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `week_day` int NULL DEFAULT NULL,
  `section` int NULL DEFAULT NULL,
  PRIMARY KEY (`arrange_id`) USING BTREE,
  INDEX `fk_arrange_course`(`course_id` ASC) USING BTREE,
  INDEX `fk_arrange_teacher`(`tid` ASC) USING BTREE,
  CONSTRAINT `fk_arrange_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_arrange_teacher` FOREIGN KEY (`tid`) REFERENCES `teacher` (`tid`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course_arrange
-- ----------------------------
INSERT INTO `course_arrange` VALUES (1, 1, 3, 'A101', 1, 1);
INSERT INTO `course_arrange` VALUES (2, 2, 4, 'B203', 1, 3);
INSERT INTO `course_arrange` VALUES (3, 3, 1, 'C305', 2, 1);
INSERT INTO `course_arrange` VALUES (4, 8, 2, 'A102', 2, 3);
INSERT INTO `course_arrange` VALUES (5, 5, 3, 'B204', 3, 1);
INSERT INTO `course_arrange` VALUES (6, 6, 5, 'A103', 3, 3);
INSERT INTO `course_arrange` VALUES (7, 7, 9, 'C306', 4, 1);
INSERT INTO `course_arrange` VALUES (8, 4, 1, 'B205', 4, 3);
INSERT INTO `course_arrange` VALUES (9, 9, 3, 'A104', 5, 1);
INSERT INTO `course_arrange` VALUES (10, 10, 7, 'B206', 5, 3);
INSERT INTO `course_arrange` VALUES (11, 8, 2, 'A104', 1, 1);
INSERT INTO `course_arrange` VALUES (12, 8, 2, 'A104', 1, 2);
INSERT INTO `course_arrange` VALUES (13, 1, 3, 'B201', 1, 4);
INSERT INTO `course_arrange` VALUES (14, 8, 2, 'A102', 2, 2);
INSERT INTO `course_arrange` VALUES (15, 5, 3, 'B202', 2, 4);
INSERT INTO `course_arrange` VALUES (16, 1, 3, 'C301', 3, 2);
INSERT INTO `course_arrange` VALUES (17, 8, 2, 'C302', 3, 3);
INSERT INTO `course_arrange` VALUES (18, 2, 4, 'A103', 3, 4);
INSERT INTO `course_arrange` VALUES (19, 8, 2, 'B203', 4, 1);
INSERT INTO `course_arrange` VALUES (20, 1, 3, 'C303', 4, 2);

-- ----------------------------
-- Table structure for department
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department`  (
  `did` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `dean` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`did`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of department
-- ----------------------------
INSERT INTO `department` VALUES (1, 'AI工程师学院', '左建国');
INSERT INTO `department` VALUES (2, '管理学院', '李明华');
INSERT INTO `department` VALUES (3, '商学院', '张佳豪');
INSERT INTO `department` VALUES (4, '食品学院', '陈秀英');

-- ----------------------------
-- Table structure for operation_log
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '??????',
  `operation` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '??????',
  `method` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '??????',
  `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '??????',
  `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP???',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 103 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of operation_log
-- ----------------------------
INSERT INTO `operation_log` VALUES (1, 'admin', '修改成绩', 'com.ggs.controller.ScoreController.updateScore', '[Score(scoreId=1, sid=1, courseId=1, usual=80.0, mid=80.0, finalScore=90.0, total=85.0, gp=3.7, studentName=null, courseName=null, clazzName=null)]', '0:0:0:0:0:0:0:1', '2026-06-17 17:22:59');
INSERT INTO `operation_log` VALUES (2, 'admin', '新增排课', 'com.ggs.controller.CourseArrangeController.add', '[CourseArrange(arrangeId=null, courseId=1, tid=3, classroom=A101, weekDay=1, section=1, courseName=null, teacherName=null)]', '0:0:0:0:0:0:0:1', '2026-06-17 17:24:27');
INSERT INTO `operation_log` VALUES (3, 'admin', '新增排课', 'com.ggs.controller.CourseArrangeController.add', '[CourseArrange(arrangeId=null, courseId=1, tid=3, classroom=A101, weekDay=1, section=1, courseName=null, teacherName=null)]', '0:0:0:0:0:0:0:1', '2026-06-17 17:24:41');
INSERT INTO `operation_log` VALUES (4, 'admin', '录入成绩', 'com.ggs.controller.ScoreController.addScore', '[Score(scoreId=41, sid=1, courseId=8, usual=91.0, mid=78.0, finalScore=78.0, total=81.9, gp=3.0, studentName=null, courseName=null, clazzName=null)]', '0:0:0:0:0:0:0:1', '2026-06-17 17:32:45');
INSERT INTO `operation_log` VALUES (5, 'guest', '修改成绩', 'com.ggs.controller.ScoreController.updateScore', '[Score(scoreId=1, sid=1, courseId=1, usual=78.0, mid=80.0, finalScore=78.0, total=78.4, gp=3.0, studentName=戴明萎, courseName=高等数学, clazzName=软工2301)]', '0:0:0:0:0:0:0:1', '2026-06-17 17:34:08');
INSERT INTO `operation_log` VALUES (6, 'admin', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-17 19:21:33');
INSERT INTO `operation_log` VALUES (7, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-17 19:21:33');
INSERT INTO `operation_log` VALUES (8, 'admin', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-17 19:21:45');
INSERT INTO `operation_log` VALUES (9, 'admin', '导出学生信息', 'com.ggs.controller.StudentController.exportStudent', '[org.springframework.web.context.request.async.StandardServletAsyncWebRequest$LifecycleHttpServletResponse@154ddf07]', '0:0:0:0:0:0:0:1', '2026-06-17 19:21:56');
INSERT INTO `operation_log` VALUES (10, 'admin', '导出成绩信息', 'com.ggs.controller.ScoreController.exportScore', '[org.springframework.web.context.request.async.StandardServletAsyncWebRequest$LifecycleHttpServletResponse@700ae879]', '0:0:0:0:0:0:0:1', '2026-06-17 19:22:02');
INSERT INTO `operation_log` VALUES (11, 'admin', '批量导入学生', 'com.ggs.controller.StudentController.importStudent', '[org.springframework.web.multipart.support.StandardMultipartHttpServletRequest$StandardMultipartFile@67424c17]', '0:0:0:0:0:0:0:1', '2026-06-17 19:23:21');
INSERT INTO `operation_log` VALUES (12, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-17 19:33:45');
INSERT INTO `operation_log` VALUES (13, 'admin', '新增排课', 'com.ggs.controller.CourseArrangeController.add', '[CourseArrange(arrangeId=null, courseId=8, tid=2, classroom=A104, weekDay=5, section=1, courseName=null, teacherName=null)]', '0:0:0:0:0:0:0:1', '2026-06-17 19:34:41');
INSERT INTO `operation_log` VALUES (14, 'admin', '新增排课', 'com.ggs.controller.CourseArrangeController.add', '[CourseArrange(arrangeId=11, courseId=8, tid=2, classroom=A104, weekDay=1, section=1, courseName=null, teacherName=null)]', '0:0:0:0:0:0:0:1', '2026-06-17 19:34:46');
INSERT INTO `operation_log` VALUES (15, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-17 19:34:49');
INSERT INTO `operation_log` VALUES (16, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-17 19:37:39');
INSERT INTO `operation_log` VALUES (17, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-17 19:39:23');
INSERT INTO `operation_log` VALUES (18, 'admin', '修改排课', 'com.ggs.controller.CourseArrangeController.update', '[CourseArrange(arrangeId=13, courseId=1, tid=3, classroom=B201, weekDay=1, section=4, courseName=null, teacherName=null)]', '0:0:0:0:0:0:0:1', '2026-06-17 19:40:06');
INSERT INTO `operation_log` VALUES (19, 'admin', '修改排课', 'com.ggs.controller.CourseArrangeController.update', '[CourseArrange(arrangeId=16, courseId=1, tid=3, classroom=C301, weekDay=3, section=2, courseName=null, teacherName=null)]', '0:0:0:0:0:0:0:1', '2026-06-17 19:40:11');
INSERT INTO `operation_log` VALUES (20, 'admin', '修改排课', 'com.ggs.controller.CourseArrangeController.update', '[CourseArrange(arrangeId=20, courseId=1, tid=3, classroom=C303, weekDay=4, section=2, courseName=null, teacherName=null)]', '0:0:0:0:0:0:0:1', '2026-06-17 19:40:19');
INSERT INTO `operation_log` VALUES (21, 'admin', '修改排课', 'com.ggs.controller.CourseArrangeController.update', '[CourseArrange(arrangeId=18, courseId=2, tid=4, classroom=A103, weekDay=3, section=4, courseName=null, teacherName=null)]', '0:0:0:0:0:0:0:1', '2026-06-17 19:40:29');
INSERT INTO `operation_log` VALUES (22, 'admin', '修改排课', 'com.ggs.controller.CourseArrangeController.update', '[CourseArrange(arrangeId=15, courseId=5, tid=3, classroom=B202, weekDay=2, section=4, courseName=null, teacherName=null)]', '0:0:0:0:0:0:0:1', '2026-06-17 19:40:46');
INSERT INTO `operation_log` VALUES (23, 'admin', '修改排课', 'com.ggs.controller.CourseArrangeController.update', '[CourseArrange(arrangeId=12, courseId=8, tid=2, classroom=A104, weekDay=1, section=2, courseName=null, teacherName=null)]', '0:0:0:0:0:0:0:1', '2026-06-17 19:40:55');
INSERT INTO `operation_log` VALUES (24, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-17 19:41:00');
INSERT INTO `operation_log` VALUES (25, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-17 19:41:18');
INSERT INTO `operation_log` VALUES (26, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-17 20:18:50');
INSERT INTO `operation_log` VALUES (27, 'admin', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-17 20:18:57');
INSERT INTO `operation_log` VALUES (28, 'admin', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-17 20:19:22');
INSERT INTO `operation_log` VALUES (29, 'admin', '修改成绩', 'com.ggs.controller.ScoreController.updateScore', '[Score(scoreId=1, sid=1, courseId=1, usual=78.0, mid=9.1, finalScore=7.8, total=29.1, gp=0.0, studentName=戴明萎, courseName=高等数学, clazzName=软工2301)]', '0:0:0:0:0:0:0:1', '2026-06-17 20:22:48');
INSERT INTO `operation_log` VALUES (30, 'admin', '修改成绩', 'com.ggs.controller.ScoreController.updateScore', '[Score(scoreId=3, sid=1, courseId=3, usual=91.0, mid=19.0, finalScore=11.0, total=36.6, gp=0.0, studentName=戴明萎, courseName=数据结构, clazzName=软工2301)]', '0:0:0:0:0:0:0:1', '2026-06-17 20:23:13');
INSERT INTO `operation_log` VALUES (31, 'admin', '修改成绩', 'com.ggs.controller.ScoreController.updateScore', '[Score(scoreId=5, sid=2, courseId=2, usual=9.1, mid=18.0, finalScore=91.0, total=51.8, gp=0.0, studentName=虚是家, courseName=大学英语, clazzName=计科2301)]', '0:0:0:0:0:0:0:1', '2026-06-17 20:23:19');
INSERT INTO `operation_log` VALUES (32, 'admin', '修改成绩', 'com.ggs.controller.ScoreController.updateScore', '[Score(scoreId=6, sid=2, courseId=3, usual=78.0, mid=78.0, finalScore=9.1, total=43.6, gp=0.0, studentName=虚是家, courseName=数据结构, clazzName=计科2301)]', '0:0:0:0:0:0:0:1', '2026-06-17 20:23:32');
INSERT INTO `operation_log` VALUES (33, 'admin', '修改成绩', 'com.ggs.controller.ScoreController.updateScore', '[Score(scoreId=7, sid=3, courseId=1, usual=91.0, mid=78.0, finalScore=7.8, total=46.8, gp=0.0, studentName=正好, courseName=高等数学, clazzName=软工2301)]', '0:0:0:0:0:0:0:1', '2026-06-17 20:23:46');
INSERT INTO `operation_log` VALUES (34, 'admin', '修改成绩', 'com.ggs.controller.ScoreController.updateScore', '[Score(scoreId=10, sid=4, courseId=1, usual=91.0, mid=78.0, finalScore=9.1, total=47.5, gp=0.0, studentName=霖金金金王玉, courseName=高等数学, clazzName=软工2301)]', '0:0:0:0:0:0:0:1', '2026-06-17 20:23:58');
INSERT INTO `operation_log` VALUES (35, 'admin', '修改成绩', 'com.ggs.controller.ScoreController.updateScore', '[Score(scoreId=37, sid=13, courseId=3, usual=83.0, mid=9.1, finalScore=61.0, total=57.2, gp=0.0, studentName=瑶瑶公主（阿水）, courseName=数据结构, clazzName=软工2301)]', '0:0:0:0:0:0:0:1', '2026-06-17 20:24:25');
INSERT INTO `operation_log` VALUES (36, 'admin', '修改成绩', 'com.ggs.controller.ScoreController.updateScore', '[Score(scoreId=41, sid=1, courseId=8, usual=91.0, mid=78.0, finalScore=78.0, total=81.9, gp=3.0, studentName=戴明萎, courseName=数据库原理, clazzName=软工2301)]', '0:0:0:0:0:0:0:1', '2026-06-17 20:24:29');
INSERT INTO `operation_log` VALUES (37, 'admin', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-17 20:24:30');
INSERT INTO `operation_log` VALUES (38, 'admin', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-17 20:24:47');
INSERT INTO `operation_log` VALUES (39, 'admin', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-17 20:24:49');
INSERT INTO `operation_log` VALUES (40, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-17 20:24:58');
INSERT INTO `operation_log` VALUES (41, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-17 20:25:15');
INSERT INTO `operation_log` VALUES (42, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-17 20:25:25');
INSERT INTO `operation_log` VALUES (43, 'admin', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-17 20:50:29');
INSERT INTO `operation_log` VALUES (44, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 09:15:23');
INSERT INTO `operation_log` VALUES (45, 'admin', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 09:16:56');
INSERT INTO `operation_log` VALUES (46, 'admin', '下载学生导入模板', 'com.ggs.controller.StudentController.downloadTemplate', '[org.springframework.web.context.request.async.StandardServletAsyncWebRequest$LifecycleHttpServletResponse@4ab02f7d]', '0:0:0:0:0:0:0:1', '2026-06-18 09:17:26');
INSERT INTO `operation_log` VALUES (47, 'admin', '下载学生导入模板', 'com.ggs.controller.StudentController.downloadTemplate', '[org.springframework.web.context.request.async.StandardServletAsyncWebRequest$LifecycleHttpServletResponse@4c2eacb1]', '0:0:0:0:0:0:0:1', '2026-06-18 09:18:08');
INSERT INTO `operation_log` VALUES (48, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 09:36:20');
INSERT INTO `operation_log` VALUES (49, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 09:37:46');
INSERT INTO `operation_log` VALUES (50, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 10:02:57');
INSERT INTO `operation_log` VALUES (51, 'admin', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 10:47:13');
INSERT INTO `operation_log` VALUES (52, 'admin', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 10:47:25');
INSERT INTO `operation_log` VALUES (53, 'admin', '导出学生信息', 'com.ggs.controller.StudentController.exportStudent', '[org.springframework.web.context.request.async.StandardServletAsyncWebRequest$LifecycleHttpServletResponse@7e382d81]', '0:0:0:0:0:0:0:1', '2026-06-18 10:47:39');
INSERT INTO `operation_log` VALUES (54, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 10:49:26');
INSERT INTO `operation_log` VALUES (55, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 11:38:26');
INSERT INTO `operation_log` VALUES (56, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 11:38:33');
INSERT INTO `operation_log` VALUES (57, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 11:38:34');
INSERT INTO `operation_log` VALUES (58, 'admin', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 11:51:04');
INSERT INTO `operation_log` VALUES (59, 'admin', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 11:51:14');
INSERT INTO `operation_log` VALUES (60, 'admin', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 11:52:27');
INSERT INTO `operation_log` VALUES (61, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 11:52:37');
INSERT INTO `operation_log` VALUES (62, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 12:17:22');
INSERT INTO `operation_log` VALUES (63, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:23:51');
INSERT INTO `operation_log` VALUES (64, 'admin', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:35:07');
INSERT INTO `operation_log` VALUES (65, 'admin', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:35:16');
INSERT INTO `operation_log` VALUES (66, 'admin', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:35:45');
INSERT INTO `operation_log` VALUES (67, 'admin', '下载学生导入模板', 'com.ggs.controller.StudentController.downloadTemplate', '[org.springframework.web.context.request.async.StandardServletAsyncWebRequest$LifecycleHttpServletResponse@64f2586c]', '0:0:0:0:0:0:0:1', '2026-06-18 13:42:28');
INSERT INTO `operation_log` VALUES (68, 'admin', '导出成绩信息', 'com.ggs.controller.ScoreController.exportScore', '[org.springframework.web.context.request.async.StandardServletAsyncWebRequest$LifecycleHttpServletResponse@6a13e196]', '0:0:0:0:0:0:0:1', '2026-06-18 13:43:57');
INSERT INTO `operation_log` VALUES (69, 'admin', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:44:17');
INSERT INTO `operation_log` VALUES (70, 'admin', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:48:00');
INSERT INTO `operation_log` VALUES (71, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:48:05');
INSERT INTO `operation_log` VALUES (72, 'zhangsan', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:48:38');
INSERT INTO `operation_log` VALUES (73, 'zhangsan', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:48:43');
INSERT INTO `operation_log` VALUES (74, 'zhangsan', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:48:48');
INSERT INTO `operation_log` VALUES (75, 'zhangsan', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:48:49');
INSERT INTO `operation_log` VALUES (76, 'zhangsan', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:49:00');
INSERT INTO `operation_log` VALUES (77, 'zhangsan', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:49:11');
INSERT INTO `operation_log` VALUES (78, 'bhl', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:49:23');
INSERT INTO `operation_log` VALUES (79, 'bhl', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:49:34');
INSERT INTO `operation_log` VALUES (80, 'bhl', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:49:35');
INSERT INTO `operation_log` VALUES (81, 'bhl', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:49:36');
INSERT INTO `operation_log` VALUES (82, 'bhl', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:49:39');
INSERT INTO `operation_log` VALUES (83, 'bhl', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:50:41');
INSERT INTO `operation_log` VALUES (84, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:50:57');
INSERT INTO `operation_log` VALUES (85, 'bhl', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:51:13');
INSERT INTO `operation_log` VALUES (86, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 13:52:11');
INSERT INTO `operation_log` VALUES (87, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 14:59:57');
INSERT INTO `operation_log` VALUES (88, 'admin', '下载学生导入模板', 'com.ggs.controller.StudentController.downloadTemplate', '[org.springframework.web.context.request.async.StandardServletAsyncWebRequest$LifecycleHttpServletResponse@4e80aa16]', '0:0:0:0:0:0:0:1', '2026-06-18 15:01:34');
INSERT INTO `operation_log` VALUES (89, 'admin', '下载学生导入模板', 'com.ggs.controller.StudentController.downloadTemplate', '[org.springframework.web.context.request.async.StandardServletAsyncWebRequest$LifecycleHttpServletResponse@37808787]', '0:0:0:0:0:0:0:1', '2026-06-18 15:01:34');
INSERT INTO `operation_log` VALUES (90, 'admin', '导出学生信息', 'com.ggs.controller.StudentController.exportStudent', '[org.springframework.web.context.request.async.StandardServletAsyncWebRequest$LifecycleHttpServletResponse@79b78c40]', '0:0:0:0:0:0:0:1', '2026-06-18 15:01:40');
INSERT INTO `operation_log` VALUES (91, 'admin', '导出学生信息', 'com.ggs.controller.StudentController.exportStudent', '[org.springframework.web.context.request.async.StandardServletAsyncWebRequest$LifecycleHttpServletResponse@72fc32dd]', '0:0:0:0:0:0:0:1', '2026-06-18 15:01:41');
INSERT INTO `operation_log` VALUES (92, 'admin', '导出学生信息', 'com.ggs.controller.StudentController.exportStudent', '[org.springframework.web.context.request.async.StandardServletAsyncWebRequest$LifecycleHttpServletResponse@c3df8d2]', '0:0:0:0:0:0:0:1', '2026-06-18 15:01:42');
INSERT INTO `operation_log` VALUES (93, 'admin', '导出学生信息', 'com.ggs.controller.StudentController.exportStudent', '[org.springframework.web.context.request.async.StandardServletAsyncWebRequest$LifecycleHttpServletResponse@7dfb5b72]', '0:0:0:0:0:0:0:1', '2026-06-18 15:01:44');
INSERT INTO `operation_log` VALUES (94, 'admin', '批量导入学生', 'com.ggs.controller.StudentController.importStudent', '[org.springframework.web.multipart.support.StandardMultipartHttpServletRequest$StandardMultipartFile@1c2cfe2]', '0:0:0:0:0:0:0:1', '2026-06-18 15:02:31');
INSERT INTO `operation_log` VALUES (95, 'admin', '录入成绩', 'com.ggs.controller.ScoreController.addScore', '[Score(scoreId=42, sid=2, courseId=4, usual=2.0, mid=2.0, finalScore=0.0, total=1.0, gp=0.0, studentName=null, courseName=null, clazzName=null)]', '0:0:0:0:0:0:0:1', '2026-06-18 15:08:37');
INSERT INTO `operation_log` VALUES (96, 'admin', '修改成绩', 'com.ggs.controller.ScoreController.updateScore', '[Score(scoreId=42, sid=2, courseId=4, usual=2.0, mid=2.0, finalScore=3.0, total=2.5, gp=0.0, studentName=虚是家, courseName=操作系统, clazzName=计科2301)]', '0:0:0:0:0:0:0:1', '2026-06-18 15:09:01');
INSERT INTO `operation_log` VALUES (97, 'admin', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 15:09:30');
INSERT INTO `operation_log` VALUES (98, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 15:12:29');
INSERT INTO `operation_log` VALUES (99, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 15:13:31');
INSERT INTO `operation_log` VALUES (100, 'admin', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 15:13:41');
INSERT INTO `operation_log` VALUES (101, 'guest', '查看数据大屏', 'com.ggs.controller.StatisticsController.dashboard', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 15:15:03');
INSERT INTO `operation_log` VALUES (102, 'guest', '查看成绩预警', 'com.ggs.controller.ScoreWarningController.list', '[]', '0:0:0:0:0:0:0:1', '2026-06-18 15:16:32');

-- ----------------------------
-- Table structure for register_request
-- ----------------------------
DROP TABLE IF EXISTS `register_request`;
CREATE TABLE `register_request`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请角色: lecturer/professor',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'pending' COMMENT 'pending/approved/rejected',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of register_request
-- ----------------------------
INSERT INTO `register_request` VALUES (1, '左飞', '$2a$10$kx4idrxiYtsZlf20wuD38.bITfDnsk.VG5Q2Lr8FIcb1YqO7ecVpu', 'professor', 'approved', '2026-06-17 10:09:18');
INSERT INTO `register_request` VALUES (2, 'guest', '$2a$10$xmBKZ3tOj0h0sooHUx159Of0YOsV1gPr/2C3uEHbk7khtn2hz5ebq', 'lecturer', 'approved', '2026-06-17 16:41:36');
INSERT INTO `register_request` VALUES (3, 'zhenghao', '$2a$10$lcwpnXL.ctZL8OyGRZZqQOvlX155pFLGIHqcr6T0pLJ3mG3Lpe2x2', 'professor', 'rejected', '2026-06-18 13:52:06');
INSERT INTO `register_request` VALUES (4, '123', '$2a$10$2zJPOGxV9ynTtxbNmXyOCuEdnReKtzahWbgj/LocpPT6L4c8VvJPu', 'professor', 'approved', '2026-06-18 15:12:11');

-- ----------------------------
-- Table structure for score
-- ----------------------------
DROP TABLE IF EXISTS `score`;
CREATE TABLE `score`  (
  `score_id` int NOT NULL AUTO_INCREMENT,
  `sid` int NULL DEFAULT NULL,
  `course_id` int NULL DEFAULT NULL,
  `usual` decimal(5, 1) NULL DEFAULT NULL,
  `mid` decimal(5, 1) NULL DEFAULT NULL,
  `final` decimal(5, 1) NULL DEFAULT NULL,
  `total` decimal(5, 1) NULL DEFAULT NULL,
  `gp` decimal(3, 1) NULL DEFAULT NULL COMMENT '???',
  PRIMARY KEY (`score_id`) USING BTREE,
  INDEX `fk_score_student`(`sid` ASC) USING BTREE,
  INDEX `fk_score_course`(`course_id` ASC) USING BTREE,
  CONSTRAINT `fk_score_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_score_student` FOREIGN KEY (`sid`) REFERENCES `student` (`sid`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of score
-- ----------------------------
INSERT INTO `score` VALUES (1, 1, 1, 78.0, 9.1, 7.8, 29.1, 0.0);
INSERT INTO `score` VALUES (2, 1, 2, 91.0, 20.0, 21.0, 41.8, 0.0);
INSERT INTO `score` VALUES (3, 1, 3, 91.0, 19.0, 11.0, 36.6, 0.0);
INSERT INTO `score` VALUES (4, 2, 1, 91.0, 18.0, 78.0, 69.9, 2.0);
INSERT INTO `score` VALUES (5, 2, 2, 9.1, 18.0, 91.0, 51.8, 0.0);
INSERT INTO `score` VALUES (6, 2, 3, 78.0, 78.0, 9.1, 43.6, 0.0);
INSERT INTO `score` VALUES (7, 3, 1, 91.0, 78.0, 7.8, 46.8, 0.0);
INSERT INTO `score` VALUES (8, 3, 2, 88.0, 85.0, 86.0, 86.3, 3.7);
INSERT INTO `score` VALUES (9, 3, 4, 82.0, 78.0, 80.0, 80.2, 3.0);
INSERT INTO `score` VALUES (10, 4, 1, 91.0, 78.0, 9.1, 47.5, 0.0);
INSERT INTO `score` VALUES (11, 4, 3, 90.0, 88.0, 89.0, 89.1, 3.7);
INSERT INTO `score` VALUES (12, 4, 5, 85.0, 80.0, 82.0, 82.3, 3.3);
INSERT INTO `score` VALUES (13, 5, 2, 92.0, 88.0, 90.0, 90.0, 4.0);
INSERT INTO `score` VALUES (14, 5, 4, 78.0, 72.0, 75.0, 75.1, 2.7);
INSERT INTO `score` VALUES (15, 5, 6, 88.0, 82.0, 85.0, 85.1, 3.7);
INSERT INTO `score` VALUES (16, 6, 1, 75.0, 68.0, 70.0, 70.6, 2.0);
INSERT INTO `score` VALUES (17, 6, 3, 82.0, 78.0, 80.0, 80.2, 3.0);
INSERT INTO `score` VALUES (18, 6, 7, 90.0, 85.0, 88.0, 88.1, 3.7);
INSERT INTO `score` VALUES (19, 7, 1, 88.0, 82.0, 85.0, 85.1, 3.7);
INSERT INTO `score` VALUES (20, 7, 2, 90.0, 88.0, 89.0, 89.1, 3.7);
INSERT INTO `score` VALUES (21, 7, 5, 82.0, 78.0, 80.0, 80.2, 3.0);
INSERT INTO `score` VALUES (22, 8, 1, 70.0, 65.0, 68.0, 67.9, 1.5);
INSERT INTO `score` VALUES (23, 8, 3, 85.0, 80.0, 82.0, 82.3, 3.3);
INSERT INTO `score` VALUES (24, 8, 4, 90.0, 88.0, 89.0, 89.1, 3.7);
INSERT INTO `score` VALUES (25, 9, 2, 78.0, 72.0, 75.0, 75.1, 2.7);
INSERT INTO `score` VALUES (26, 9, 5, 88.0, 82.0, 85.0, 85.1, 3.7);
INSERT INTO `score` VALUES (27, 9, 6, 92.0, 88.0, 90.0, 90.0, 4.0);
INSERT INTO `score` VALUES (28, 10, 1, 85.0, 80.0, 82.0, 82.3, 3.3);
INSERT INTO `score` VALUES (29, 10, 4, 90.0, 85.0, 88.0, 88.1, 3.7);
INSERT INTO `score` VALUES (30, 10, 7, 82.0, 78.0, 80.0, 80.2, 3.0);
INSERT INTO `score` VALUES (31, 11, 2, 88.0, 82.0, 85.0, 85.1, 3.7);
INSERT INTO `score` VALUES (32, 11, 3, 75.0, 70.0, 72.0, 72.1, 2.3);
INSERT INTO `score` VALUES (33, 11, 8, 90.0, 85.0, 88.0, 88.1, 3.7);
INSERT INTO `score` VALUES (34, 12, 1, 92.0, 88.0, 90.0, 90.0, 4.0);
INSERT INTO `score` VALUES (35, 12, 5, 85.0, 80.0, 82.0, 82.3, 3.3);
INSERT INTO `score` VALUES (36, 12, 6, 78.0, 72.0, 75.0, 75.1, 2.7);
INSERT INTO `score` VALUES (37, 13, 3, 83.0, 9.1, 61.0, 57.2, 0.0);
INSERT INTO `score` VALUES (38, 13, 7, 90.0, 88.0, 89.0, 89.1, 3.7);
INSERT INTO `score` VALUES (39, 13, 8, 82.0, 78.0, 80.0, 80.2, 3.0);
INSERT INTO `score` VALUES (40, 14, 2, 85.0, 80.0, 82.0, 82.3, 3.3);
INSERT INTO `score` VALUES (41, 1, 8, 91.0, 78.0, 78.0, 81.9, 3.0);
INSERT INTO `score` VALUES (42, 2, 4, 2.0, 2.0, 3.0, 2.5, 0.0);

-- ----------------------------
-- Table structure for student
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student`  (
  `sid` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `age` int NULL DEFAULT NULL,
  `sex` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cid` int NULL DEFAULT NULL,
  PRIMARY KEY (`sid`) USING BTREE,
  INDEX `fk_student_clazz`(`cid` ASC) USING BTREE,
  CONSTRAINT `fk_student_clazz` FOREIGN KEY (`cid`) REFERENCES `clazz` (`cid`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 42 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student
-- ----------------------------
INSERT INTO `student` VALUES (1, '戴明萎', 20, '男', 3);
INSERT INTO `student` VALUES (2, '虚是家', 19, '女', 1);
INSERT INTO `student` VALUES (3, '正好', 21, '男', 3);
INSERT INTO `student` VALUES (4, '霖金金金王玉', 20, '男', 3);
INSERT INTO `student` VALUES (5, '陈晴', 19, '女', 5);
INSERT INTO `student` VALUES (6, '杨帆', 20, '男', 1);
INSERT INTO `student` VALUES (7, '赵敏', 19, '女', 2);
INSERT INTO `student` VALUES (8, '黄磊', 21, '男', 2);
INSERT INTO `student` VALUES (9, '周杰', 20, '男', 2);
INSERT INTO `student` VALUES (10, '吴倩', 19, '女', 2);
INSERT INTO `student` VALUES (11, '徐鹏', 20, '男', 2);
INSERT INTO `student` VALUES (12, '孙丽', 19, '女', 2);
INSERT INTO `student` VALUES (13, '瑶瑶公主（阿水）', 21, '男', 3);
INSERT INTO `student` VALUES (14, '朱迪', 20, '女', 3);
INSERT INTO `student` VALUES (15, '累军', 20, '男', 3);
INSERT INTO `student` VALUES (16, '郭芳', 19, '女', 3);
INSERT INTO `student` VALUES (17, '何伟', 21, '男', 4);
INSERT INTO `student` VALUES (18, '高燕', 20, '女', 4);
INSERT INTO `student` VALUES (19, '林涛', 20, '男', 4);
INSERT INTO `student` VALUES (20, '郑欣', 19, '女', 4);
INSERT INTO `student` VALUES (21, '谢峰', 21, '男', 4);
INSERT INTO `student` VALUES (22, '罗丹', 20, '女', 4);
INSERT INTO `student` VALUES (23, '梁辉', 20, '男', 5);
INSERT INTO `student` VALUES (24, '宋洁', 19, '女', 5);
INSERT INTO `student` VALUES (25, '唐勇', 21, '男', 5);
INSERT INTO `student` VALUES (26, '韩梅', 20, '女', 5);
INSERT INTO `student` VALUES (27, '曹阳', 20, '男', 5);
INSERT INTO `student` VALUES (30, '邓婷', 20, '女', 5);
INSERT INTO `student` VALUES (31, '测试学生', 20, '男', 1);
INSERT INTO `student` VALUES (32, '陈明', 20, '男', 1);
INSERT INTO `student` VALUES (33, '林晓', 19, '女', 1);
INSERT INTO `student` VALUES (34, '周杰', 21, '男', 2);
INSERT INTO `student` VALUES (35, '吴芳', 20, '女', 2);
INSERT INTO `student` VALUES (36, '郑涛', 19, '男', 3);
INSERT INTO `student` VALUES (37, '王菲', 20, '女', 3);
INSERT INTO `student` VALUES (38, '李强', 22, '男', 4);
INSERT INTO `student` VALUES (39, '赵敏', 20, '女', 4);
INSERT INTO `student` VALUES (40, '孙浩', 21, '男', 5);
INSERT INTO `student` VALUES (41, '刘婷', 19, '女', 5);

-- ----------------------------
-- Table structure for teacher
-- ----------------------------
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher`  (
  `tid` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `age` int NULL DEFAULT NULL,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `department` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`tid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of teacher
-- ----------------------------
INSERT INTO `teacher` VALUES (1, '左建国', 52, '教授', 'AI工程师学院');
INSERT INTO `teacher` VALUES (2, '范范范', 18, '副教授', 'AI工程师学院');
INSERT INTO `teacher` VALUES (3, '张明德', 48, '教授', '管理学院');
INSERT INTO `teacher` VALUES (4, '刘慧敏', 39, '讲师', '管理学院');
INSERT INTO `teacher` VALUES (5, '陈志远', 36, '讲师', '商学院');
INSERT INTO `teacher` VALUES (6, '周丽华', 42, '副教授', '商学院');
INSERT INTO `teacher` VALUES (7, '赵文轩', 50, '教授', '食品学院');
INSERT INTO `teacher` VALUES (8, '孙雅琴', 36, '讲师', '食品学院');
INSERT INTO `teacher` VALUES (9, '吴浩然', 44, '副教授', 'AI工程师学院');
INSERT INTO `teacher` VALUES (10, '郑思琪', 38, '讲师', '管理学院');
INSERT INTO `teacher` VALUES (11, '张佳豪', 21, '教授', '商学院');

SET FOREIGN_KEY_CHECKS = 1;
