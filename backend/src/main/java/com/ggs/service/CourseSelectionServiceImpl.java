package com.ggs.service;

import com.ggs.exception.BusinessException;
import com.ggs.interceptor.JwtInterceptor;
import com.ggs.mapper.CourseArrangeMapper;
import com.ggs.mapper.CourseSelectionMapper;
import com.ggs.pojo.CourseArrange;
import com.ggs.pojo.CourseSelection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CourseSelectionServiceImpl implements CourseSelectionService {

    @Autowired
    private CourseSelectionMapper selectionMapper;

    @Autowired
    private CourseArrangeMapper arrangeMapper;

    @Override
    public List<CourseSelection> listAll() {
        return selectionMapper.listAll();
    }

    @Override
    public List<CourseSelection> listByStudent(Integer sid) {
        return selectionMapper.listByStudent(sid);
    }

    @Override
    public void add(Integer sid, Integer courseId) {
        if (sid == null || courseId == null) {
            throw new BusinessException("请选择学生和课程");
        }
        if (selectionMapper.countStudent(sid) == 0) {
            throw new BusinessException("学生不存在");
        }
        if (selectionMapper.countCourse(courseId) == 0) {
            throw new BusinessException("课程不存在");
        }
        if (selectionMapper.countBySidAndCourse(sid, courseId) > 0) {
            throw new BusinessException("该学生已选此课程，请勿重复选课");
        }

        // 排课时间冲突检测：同一学生不能同时段选修两门课
        List<CourseArrange> newArranges = arrangeMapper.listByCourse(courseId);
        List<Map<String, Object>> occupied = selectionMapper.listConflictSlots(sid);
        Set<String> occupiedSlots = new HashSet<>();
        Map<String, String> slotCourseName = new HashMap<>();
        for (Map<String, Object> row : occupied) {
            Object wd = row.get("weekDay");
            Object sec = row.get("section");
            if (wd == null || sec == null) {
                continue;
            }
            String key = wd + "-" + sec;
            occupiedSlots.add(key);
            Object courseName = row.get("courseName");
            slotCourseName.putIfAbsent(key, courseName == null ? "未知课程" : String.valueOf(courseName));
        }
        for (CourseArrange arrange : newArranges) {
            if (arrange.getWeekDay() == null || arrange.getSection() == null) {
                continue;
            }
            String key = arrange.getWeekDay() + "-" + arrange.getSection();
            if (occupiedSlots.contains(key)) {
                throw new BusinessException("选课冲突：该课程排课时间与学生已选课程「"
                        + slotCourseName.get(key) + "」重叠");
            }
        }

        CourseSelection selection = new CourseSelection();
        selection.setSid(sid);
        selection.setCourseId(courseId);
        Long uid = JwtInterceptor.UID_HOLDER.get();
        selection.setUid(uid == null ? null : uid.intValue());
        selection.setCreateTime(LocalDateTime.now());
        selectionMapper.insert(selection);
    }

    @Override
    public void delete(Integer id) {
        if (id == null || selectionMapper.delete(id) == 0) {
            throw new BusinessException("删除失败，选课记录不存在");
        }
    }
}