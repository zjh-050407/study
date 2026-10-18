package com.ggs.service;

import com.ggs.pojo.Score;

import java.util.List;
import java.util.Map;

public interface ScoreService {
    List<Score> listScore();
    List<Score> listByStudent(Integer sid);
    List<Score> listByCourse(Integer courseId);
    int add(Score score);
    int update(Score score);
    int delete(Integer scoreId);
    List<Map<String, Object>> avgByCourse();
    List<Map<String, Object>> countByGrade();
}
