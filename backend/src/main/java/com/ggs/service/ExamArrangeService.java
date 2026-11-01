package com.ggs.service;

import com.ggs.pojo.ExamArrange;

import java.util.List;
import java.util.Map;

public interface ExamArrangeService {

    List<ExamArrange> listAll();

    void add(ExamArrange exam);

    void update(ExamArrange exam);

    void delete(Integer id);

    /** 学生本人的考试安排 */
    List<Map<String, Object>> myExams(Integer sid);
}