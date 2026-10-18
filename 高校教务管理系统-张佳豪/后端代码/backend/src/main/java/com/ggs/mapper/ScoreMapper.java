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
    List<Map<String, Object>> avgByCourse();
    List<Map<String, Object>> countByGrade();
}
