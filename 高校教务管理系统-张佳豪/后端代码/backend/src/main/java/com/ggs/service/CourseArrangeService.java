package com.ggs.service;

import com.ggs.pojo.CourseArrange;

import java.util.List;

public interface CourseArrangeService {
    List<CourseArrange> listArrange();
    int add(CourseArrange arrange);
    int update(CourseArrange arrange);
    int delete(Integer arrangeId);
    List<CourseArrange> listByTeacher(Integer tid);

    /**
     * 检查排课冲突
     * @return 冲突描述，无冲突返回 null
     */
    String checkConflict(CourseArrange arrange);
}
