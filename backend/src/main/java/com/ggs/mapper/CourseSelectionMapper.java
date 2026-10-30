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
}