package com.ggs.mapper;

import com.ggs.pojo.Score;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ScoreMapper {
    List<Score> listScore();
    List<Score> listByStudent(@Param("sid") Integer sid);
    List<Score> listByCourse(@Param("courseId") Integer courseId);
    int add(Score score);
    int update(Score score);
    int delete(@Param("scoreId") Integer scoreId);
    int countByStudentAndCourse(@Param("sid") Integer sid,
                                @Param("courseId") Integer courseId,
                                @Param("semesterId") Integer semesterId,
                                @Param("excludeId") Integer excludeId);
    List<Map<String, Object>> avgByCourse();
    List<Map<String, Object>> countByGrade();

    /**
     * 学生按学期查询成绩；semesterId=0 表示未分学期（semester_id IS NULL），null 表示全部学期
     */
    List<Score> listByStudentAndSemester(@Param("sid") Integer sid, @Param("semesterId") Integer semesterId);
}
