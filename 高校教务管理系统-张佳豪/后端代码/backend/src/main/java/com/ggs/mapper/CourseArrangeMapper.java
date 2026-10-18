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

    int countByCourse(@Param("courseId") Integer courseId);

    /**
     * 查询与指定条件冲突的排课数量（排除自身）
     */
    int countConflict(@Param("arrangeId") Integer arrangeId,
                      @Param("classroom") String classroom,
                      @Param("weekDay") Integer weekDay,
                      @Param("section") Integer section,
                      @Param("tid") Integer tid);
}
