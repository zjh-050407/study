package com.ggs.mapper;

import com.ggs.pojo.CourseSelection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CourseSelectionMapper {
    List<CourseSelection> listAll();

    List<CourseSelection> listByStudent(@Param("sid") Integer sid);

    int insert(CourseSelection selection);

    int delete(@Param("id") Integer id);

    int countBySidAndCourse(@Param("sid") Integer sid, @Param("courseId") Integer courseId);

    int countStudent(@Param("sid") Integer sid);

    int countCourse(@Param("courseId") Integer courseId);

    /**
     * 查询某学生已选课程的排课时间（courseId/courseName/weekDay/section），
     * 用于选课冲突检测。无排课记录的课程不会出现在结果中。
     */
    List<Map<String, Object>> listConflictSlots(@Param("sid") Integer sid);

    /**
     * 查询学生当前已选的课程编号集合（用于会话内校验）
     */
    List<Integer> listCourseIdsByStudent(@Param("sid") Integer sid);

    /**
     * 课程列表（带该学生是否已选标记与任课教师汇总）
     */
    List<Map<String, Object>> listCourseOptions(@Param("sid") Integer sid);

    /**
     * 学生课表槽位：选课 × 排课（week_day + section），semesterId 为 null 时不过滤学期
     */
    List<Map<String, Object>> listScheduleSlots(@Param("sid") Integer sid, @Param("semesterId") Integer semesterId);

    /**
     * 按学生+课程删除选课记录（退选审批通过时使用）
     */
    int deleteBySidAndCourse(@Param("sid") Integer sid, @Param("courseId") Integer courseId);
}