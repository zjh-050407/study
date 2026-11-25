package com.ggs.controller;

import com.ggs.annotation.OperationLogAnnotation;
import com.ggs.exception.BusinessException;
import com.ggs.mapper.SemesterMapper;
import com.ggs.mapper.TeachingWeekMapper;
import com.ggs.pojo.Semester;
import com.ggs.pojo.TeachingWeek;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 校历（教学周历）管理：教授/管理员维护，学生只读。
 */
@RestController
@RequestMapping("/calendar")
public class CalendarController {

    @Autowired
    private TeachingWeekMapper weekMapper;

    @Autowired
    private SemesterMapper semesterMapper;

    /** 解析学期：未传默认当前学期；0 表示未分学期哨兵 */
    private Integer resolveSemester(Integer semesterId) {
        if (semesterId != null) {
            return semesterId;
        }
        Semester current = semesterMapper.getCurrent();
        return current == null ? 0 : current.getId();
    }

    @GetMapping("/list")
    public ResultVO list(@RequestParam(required = false) Integer semesterId) {
        return ResultVO.success(weekMapper.list(resolveSemester(semesterId)));
    }

    @OperationLogAnnotation("新增校历周次")
    @PostMapping
    public ResultVO add(@RequestBody TeachingWeek week) {
        RoleCheckUtil.requireRole("professor", "admin");
        validate(week);
        if (week.getSemesterId() == null) {
            week.setSemesterId(0);
        }
        weekMapper.insert(week);
        return ResultVO.success();
    }

    @OperationLogAnnotation("修改校历周次")
    @PutMapping
    public ResultVO update(@RequestBody TeachingWeek week) {
        RoleCheckUtil.requireRole("professor", "admin");
        if (week.getId() == null) {
            throw new BusinessException("缺少ID");
        }
        validate(week);
        weekMapper.update(week);
        return ResultVO.success();
    }

    @OperationLogAnnotation("删除校历周次")
    @DeleteMapping("/{id}")
    public ResultVO delete(@PathVariable Integer id) {
        RoleCheckUtil.requireRole("professor", "admin");
        weekMapper.delete(id);
        return ResultVO.success();
    }

    private void validate(TeachingWeek week) {
        if (week.getWeekNo() == null || week.getWeekNo() < 1 || week.getWeekNo() > 30) {
            throw new BusinessException("教学周必须在 1-30 之间");
        }
        if (week.getStartDate() != null && week.getEndDate() != null
                && week.getEndDate().isBefore(week.getStartDate())) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
    }
}
