package com.ggs.service;

import com.ggs.exception.BusinessException;
import com.ggs.mapper.SemesterMapper;
import com.ggs.mapper.TeachTaskMapper;
import com.ggs.mapper.TeacherMapper;
import com.ggs.mapper.TrainingPlanMapper;
import com.ggs.pojo.PlanCourse;
import com.ggs.pojo.Semester;
import com.ggs.pojo.TeachTask;
import com.ggs.pojo.TrainingPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeachTaskServiceImpl implements TeachTaskService {

    @Autowired
    private TeachTaskMapper teachTaskMapper;

    @Autowired
    private TrainingPlanMapper planMapper;

    @Autowired
    private SemesterMapper semesterMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Override
    public int generate() {
        Semester current = semesterMapper.getCurrent();
        if (current == null) {
            throw new BusinessException("未设置当前学期，无法生成教学任务");
        }
        List<TrainingPlan> plans = planMapper.listPlans();
        int created = 0;
        for (TrainingPlan plan : plans) {
            List<PlanCourse> courses = planMapper.listCourses(plan.getId());
            for (PlanCourse pc : courses) {
                if (pc.getCourseId() == null) {
                    continue;
                }
                // 幂等：同一课程+班级+学期已存在则跳过
                if (teachTaskMapper.countByCourseCidSem(pc.getCourseId(), plan.getCid(), current.getId()) > 0) {
                    continue;
                }
                TeachTask task = new TeachTask();
                task.setCourseId(pc.getCourseId());
                task.setSemesterId(current.getId());
                task.setCid(plan.getCid());
                task.setStatus(0);
                teachTaskMapper.insert(task);
                created++;
            }
        }
        return created;
    }

    @Override
    public List<TeachTask> listAll() {
        return teachTaskMapper.listAll();
    }

    @Override
    public List<TeachTask> listUndone() {
        return teachTaskMapper.listUndone();
    }

    @Override
    public void assignTeacher(Integer id, Integer tid) {
        if (id == null || teachTaskMapper.getById(id) == null) {
            throw new BusinessException("教学任务不存在");
        }
        if (tid != null && teacherMapper.getTeacherById(Long.valueOf(tid)) == null) {
            throw new BusinessException("教师不存在");
        }
        teachTaskMapper.updateTid(id, tid);
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        if (id == null || teachTaskMapper.getById(id) == null) {
            throw new BusinessException("教学任务不存在");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("任务状态仅支持：0=未排课 1=已排课");
        }
        teachTaskMapper.updateStatus(id, status);
    }

    @Override
    public void delete(Integer id) {
        if (id == null || teachTaskMapper.delete(id) == 0) {
            throw new BusinessException("删除失败，教学任务不存在");
        }
    }
}
