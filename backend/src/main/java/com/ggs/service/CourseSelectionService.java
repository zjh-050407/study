package com.ggs.service;

import com.ggs.pojo.CourseSelection;

import java.util.List;
import java.util.Map;

public interface CourseSelectionService {
    List<CourseSelection> listAll();

    List<CourseSelection> listByStudent(Integer sid);

    void add(Integer sid, Integer courseId);

    void delete(Integer id);

    /** 课程列表（带该学生是否已选标记） */
    List<Map<String, Object>> listCourseOptions(Integer sid);

    /** 学生课表槽位（选课×排课），semesterId 为 null 时不过滤学期 */
    List<Map<String, Object>> listScheduleSlots(Integer sid, Integer semesterId);
}