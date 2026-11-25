package com.ggs.mapper;

import com.ggs.pojo.CourseArrange;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseArrangeMapper {
    List<CourseArrange> listArrange();
    int add(CourseArrange arrange);
    int update(CourseArrange arrange);
    int delete(@Param("arrangeId") Integer arrangeId);
    List<CourseArrange> listByTeacher(@Param("tid") Integer tid);

    List<CourseArrange> listByCourse(@Param("courseId") Integer courseId);

    int countByCourse(@Param("courseId") Integer courseId);

    /**
     * 查询与指定条件冲突的排课数量（排除自身）
     */
    int countConflict(@Param("arrangeId") Integer arrangeId,
                      @Param("classroom") String classroom,
                      @Param("weekDay") Integer weekDay,
                      @Param("section") Integer section,
                      @Param("tid") Integer tid);

    /** 教师课表 grid（批次M） */
    java.util.List<java.util.Map<String, Object>> listGridByTeacher(@Param("tid") Integer tid,
                                                                   @Param("semesterId") Integer semesterId);

    /** 教室课表 grid（批次M） */
    java.util.List<java.util.Map<String, Object>> listGridByClassroom(@Param("classroom") String classroom,
                                                                     @Param("semesterId") Integer semesterId);
}
