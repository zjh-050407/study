package com.ggs.controller;

import com.ggs.interceptor.JwtInterceptor;
import com.ggs.mapper.SemesterMapper;
import com.ggs.pojo.Semester;
import com.ggs.service.CourseSelectionService;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private CourseSelectionService selectionService;

    @Autowired
    private SemesterMapper semesterMapper;

    /** 个人课表：选课×排课生成周视图，默认按当前学期（旧数据兼容） */
    @GetMapping("/my")
    public ResultVO my() {
        RoleCheckUtil.requireRole("student");
        Long uid = JwtInterceptor.UID_HOLDER.get();
        int sid = uid == null ? -1 : uid.intValue();
        Semester current = semesterMapper.getCurrent();
        Integer semesterId = current == null ? null : current.getId();
        return ResultVO.success(selectionService.listScheduleSlots(sid, semesterId));
    }
}