package com.ggs.service;

import com.ggs.mapper.CourseArrangeMapper;
import com.ggs.pojo.CourseArrange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CourseArrangeServiceImpl implements CourseArrangeService {

    @Autowired
    private CourseArrangeMapper courseArrangeMapper;

    @Override
    public List<CourseArrange> listArrange() {
        return courseArrangeMapper.listArrange();
    }

    @Override
    public int add(CourseArrange arrange) {
        return courseArrangeMapper.add(arrange);
    }

    @Override
    public int update(CourseArrange arrange) {
        return courseArrangeMapper.update(arrange);
    }

    @Override
    public int delete(Integer arrangeId) {
        return courseArrangeMapper.delete(arrangeId);
    }

    @Override
    public List<CourseArrange> listByTeacher(Integer tid) {
        return courseArrangeMapper.listByTeacher(tid);
    }

    @Override
    public String checkConflict(CourseArrange arrange) {
        if (arrange == null || !StringUtils.hasText(arrange.getClassroom())
                || arrange.getWeekDay() == null || arrange.getSection() == null
                || arrange.getTid() == null) {
            return "排课信息不完整，无法检测冲突";
        }
        int conflictCount = courseArrangeMapper.countConflict(
                arrange.getArrangeId(),
                arrange.getClassroom(),
                arrange.getWeekDay(),
                arrange.getSection(),
                arrange.getTid()
        );
        if (conflictCount > 0) {
            return "检测到排课冲突：该时间段的教室或授课教师已被占用";
        }
        return null;
    }
}
