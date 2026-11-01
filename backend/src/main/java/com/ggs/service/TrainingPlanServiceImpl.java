package com.ggs.service;

import com.ggs.exception.BusinessException;
import com.ggs.mapper.ClazzMapper;
import com.ggs.mapper.CourseMapper;
import com.ggs.mapper.TrainingPlanMapper;
import com.ggs.pojo.PlanCourse;
import com.ggs.pojo.TrainingPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainingPlanServiceImpl implements TrainingPlanService {

    private static final List<String> COURSE_TYPES = List.of("必修", "限选", "任选");

    @Autowired
    private TrainingPlanMapper planMapper;

    @Autowired
    private ClazzMapper clazzMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Override
    public List<TrainingPlan> listPlans() {
        return planMapper.listPlans();
    }

    @Override
    public void createPlan(TrainingPlan plan) {
        validatePlan(plan);
        if (planMapper.countByCid(plan.getCid(), null) > 0) {
            throw new BusinessException("该班级已有培养方案，请勿重复创建");
        }
        planMapper.insert(plan);
    }

    @Override
    public void updatePlan(TrainingPlan plan) {
        if (plan.getId() == null) {
            throw new BusinessException("参数错误：缺少方案ID");
        }
        validatePlan(plan);
        if (planMapper.countByCid(plan.getCid(), plan.getId()) > 0) {
            throw new BusinessException("该班级已有培养方案，请勿重复创建");
        }
        planMapper.update(plan);
    }

    private void validatePlan(TrainingPlan plan) {
        if (plan.getCid() == null || clazzMapper.getById(plan.getCid()) == null) {
            throw new BusinessException("请选择有效的班级");
        }
        String name = plan.getName() == null ? "" : plan.getName().trim();
        if (!name.isEmpty() && name.length() > 100) {
            throw new BusinessException("方案名称不能超过100字");
        }
        plan.setName(name.isEmpty() ? null : name);
        Double total = orZero(plan.getTotalCreditRequired());
        Double required = orZero(plan.getRequiredCreditRequired());
        Double elective = orZero(plan.getElectiveCreditRequired());
        if (total < 0 || required < 0 || elective < 0) {
            throw new BusinessException("学分要求不能为负数");
        }
        plan.setTotalCreditRequired(total);
        plan.setRequiredCreditRequired(required);
        plan.setElectiveCreditRequired(elective);
    }

    @Override
    public void deletePlan(Integer id) {
        if (id == null || planMapper.getById(id) == null) {
            throw new BusinessException("培养方案不存在");
        }
        planMapper.deleteCoursesByPlan(id);
        planMapper.delete(id);
    }

    @Override
    public List<PlanCourse> listCourses(Integer planId) {
        return planMapper.listCourses(planId);
    }

    @Override
    public void addCourse(PlanCourse course) {
        Integer planId = course.getPlanId();
        if (planId == null || planMapper.getById(planId) == null) {
            throw new BusinessException("培养方案不存在");
        }
        validateCourse(course);
        if (planMapper.countCourse(planId, course.getCourseId(), null) > 0) {
            throw new BusinessException("该课程已在培养方案中，请勿重复添加");
        }
        planMapper.insertCourse(course);
    }

    @Override
    public void updateCourse(PlanCourse course) {
        if (course.getId() == null) {
            throw new BusinessException("参数错误：缺少方案课程ID");
        }
        validateCourse(course);
        planMapper.updateCourse(course);
    }

    private void validateCourse(PlanCourse course) {
        if (course.getCourseId() == null || courseMapper.getById(course.getCourseId()) == null) {
            throw new BusinessException("请选择有效课程");
        }
        String type = course.getType() == null ? "" : course.getType().trim();
        if (!COURSE_TYPES.contains(type)) {
            throw new BusinessException("课程类型仅支持：必修/限选/任选");
        }
        course.setType(type);
        Double credit = orZero(course.getRequireCredit());
        if (credit < 0) {
            throw new BusinessException("学分要求不能为负数");
        }
        course.setRequireCredit(credit);
    }

    @Override
    public void deleteCourse(Integer id) {
        if (id == null || planMapper.deleteCourse(id) == 0) {
            throw new BusinessException("方案课程不存在");
        }
    }

    private Double orZero(Double v) {
        return v == null ? 0.0 : v;
    }
}