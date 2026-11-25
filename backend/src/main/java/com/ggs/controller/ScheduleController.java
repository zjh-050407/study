package com.ggs.controller;

import com.ggs.exception.BusinessException;
import com.ggs.interceptor.JwtInterceptor;
import com.ggs.mapper.CourseArrangeMapper;
import com.ggs.mapper.SemesterMapper;
import com.ggs.mapper.TeacherMapper;
import com.ggs.pojo.Semester;
import com.ggs.pojo.Teacher;
import com.ggs.service.CourseSelectionService;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private CourseSelectionService selectionService;

    @Autowired
    private SemesterMapper semesterMapper;

    @Autowired
    private CourseArrangeMapper arrangeMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    private Integer currentSemesterId() {
        Semester current = semesterMapper.getCurrent();
        return current == null ? null : current.getId();
    }

    /** 个人课表：选课×排课生成周视图，默认按当前学期（旧数据兼容） */
    @GetMapping("/my")
    public ResultVO my() {
        RoleCheckUtil.requireRole("student");
        Long uid = JwtInterceptor.UID_HOLDER.get();
        int sid = uid == null ? -1 : uid.intValue();
        return ResultVO.success(selectionService.listScheduleSlots(sid, currentSemesterId()));
    }

    /** 学生课表 grid：学生本人；管理员/教授可指定学号查看 */
    @GetMapping("/grid/student")
    public ResultVO gridStudent(@RequestParam(required = false) Integer sid) {
        String role = JwtInterceptor.ROLE_HOLDER.get();
        Long uid = JwtInterceptor.UID_HOLDER.get();
        int targetSid;
        if ("student".equals(role)) {
            targetSid = uid == null ? -1 : uid.intValue();
        } else if ("admin".equals(role) || "professor".equals(role)) {
            if (sid == null) {
                throw new BusinessException("缺少学生学号");
            }
            targetSid = sid;
        } else {
            throw new BusinessException("无权限查看学生课表");
        }
        return ResultVO.success(selectionService.listScheduleSlots(targetSid, currentSemesterId()));
    }

    /** 教师课表 grid：教师本人（按登录名关联教师档案）；管理员/教授可指定教师查看 */
    @GetMapping("/grid/teacher")
    public ResultVO gridTeacher(@RequestParam(required = false) Integer tid) {
        String role = JwtInterceptor.ROLE_HOLDER.get();
        Integer targetTid = tid;
        if ("admin".equals(role) || "professor".equals(role)) {
            if (targetTid == null) {
                targetTid = resolveTeacherTid();
            }
        } else if ("lecturer".equals(role)) {
            targetTid = resolveTeacherTid();
        } else {
            throw new BusinessException("无权限查看教师课表");
        }
        if (targetTid == null) {
            throw new BusinessException("找不到关联的教师档案");
        }
        return ResultVO.success(arrangeMapper.listGridByTeacher(targetTid, currentSemesterId()));
    }

    /** 教室课表 grid：教授/管理员 */
    @GetMapping("/grid/classroom")
    public ResultVO gridClassroom(@RequestParam String classroom) {
        RoleCheckUtil.requireRole("professor", "admin");
        if (classroom == null || classroom.trim().isEmpty()) {
            throw new BusinessException("缺少教室");
        }
        return ResultVO.success(arrangeMapper.listGridByClassroom(classroom.trim(), currentSemesterId()));
    }

    /** 按登录用户名关联教师档案 */
    private Integer resolveTeacherTid() {
        String username = JwtInterceptor.USERNAME_HOLDER.get();
        Teacher teacher = teacherMapper.getByUsername(username);
        return teacher == null ? null : teacher.getTid();
    }
}
