package com.ggs.controller;

import com.ggs.annotation.OperationLogAnnotation;
import com.ggs.interceptor.JwtInterceptor;
import com.ggs.mapper.ExamArrangeMapper;
import com.ggs.pojo.ExamArrange;
import com.ggs.service.ExamArrangeService;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exam")
public class ExamController {

    @Autowired
    private ExamArrangeService examService;

    @Autowired
    private ExamArrangeMapper examMapper;

    /* ==================== 教师/管理员：考务排考 ==================== */

    @GetMapping("/list")
    public ResultVO list(@RequestParam(required = false) Integer page,
                         @RequestParam(required = false) Integer size) {
        RoleCheckUtil.requireRole("professor", "admin");
        if (page != null && size != null) {
            int offset = (page - 1) * size;
            List<ExamArrange> list = examMapper.listByPage(offset, size);
            int total = examMapper.countAll();
            return ResultVO.success(new ResultVO.PageResult<>(list, total, page, size));
        }
        return ResultVO.success(examService.listAll());
    }

    @OperationLogAnnotation("新增考试安排")
    @PostMapping
    public ResultVO add(@RequestBody ExamArrange exam) {
        RoleCheckUtil.requireRole("professor", "admin");
        examService.add(exam);
        return ResultVO.success();
    }

    @OperationLogAnnotation("修改考试安排")
    @PutMapping
    public ResultVO update(@RequestBody ExamArrange exam) {
        RoleCheckUtil.requireRole("professor", "admin");
        examService.update(exam);
        return ResultVO.success();
    }

    @OperationLogAnnotation("删除考试安排")
    @DeleteMapping("/{id}")
    public ResultVO delete(@PathVariable Integer id) {
        RoleCheckUtil.requireRole("professor", "admin");
        examService.delete(id);
        return ResultVO.success();
    }

    /* ==================== 学生端：我的考试安排 ==================== */

    @GetMapping("/my")
    public ResultVO my() {
        RoleCheckUtil.requireRole("student");
        Long uid = JwtInterceptor.UID_HOLDER.get();
        int sid = uid == null ? -1 : uid.intValue();
        return ResultVO.success(examService.myExams(sid));
    }
}