package com.ggs.controller;

import com.ggs.annotation.OperationLogAnnotation;
import com.ggs.pojo.PlanCourse;
import com.ggs.pojo.TrainingPlan;
import com.ggs.service.TrainingPlanService;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/training")
public class TrainingPlanController {

    @Autowired
    private TrainingPlanService planService;

    @GetMapping("/plan/list")
    public ResultVO list() {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(planService.listPlans());
    }

    @OperationLogAnnotation("新增培养方案")
    @PostMapping("/plan")
    public ResultVO create(@RequestBody TrainingPlan plan) {
        RoleCheckUtil.requireRole("professor", "admin");
        planService.createPlan(plan);
        return ResultVO.success();
    }

    @OperationLogAnnotation("编辑培养方案")
    @PutMapping("/plan")
    public ResultVO update(@RequestBody TrainingPlan plan) {
        RoleCheckUtil.requireRole("professor", "admin");
        planService.updatePlan(plan);
        return ResultVO.success();
    }

    @OperationLogAnnotation("删除培养方案")
    @DeleteMapping("/plan/{id}")
    public ResultVO delete(@PathVariable Integer id) {
        RoleCheckUtil.requireRole("professor", "admin");
        planService.deletePlan(id);
        return ResultVO.success();
    }

    @GetMapping("/plan/{id}/courses")
    public ResultVO courses(@PathVariable Integer id) {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(planService.listCourses(id));
    }

    @OperationLogAnnotation("培养方案添加课程")
    @PostMapping("/plan/course")
    public ResultVO addCourse(@RequestBody PlanCourse course) {
        RoleCheckUtil.requireRole("professor", "admin");
        planService.addCourse(course);
        return ResultVO.success();
    }

    @OperationLogAnnotation("培养方案课程修改")
    @PutMapping("/plan/course")
    public ResultVO updateCourse(@RequestBody PlanCourse course) {
        RoleCheckUtil.requireRole("professor", "admin");
        planService.updateCourse(course);
        return ResultVO.success();
    }

    @OperationLogAnnotation("培养方案课程删除")
    @DeleteMapping("/plan/course/{id}")
    public ResultVO deleteCourse(@PathVariable Integer id) {
        RoleCheckUtil.requireRole("professor", "admin");
        planService.deleteCourse(id);
        return ResultVO.success();
    }
}