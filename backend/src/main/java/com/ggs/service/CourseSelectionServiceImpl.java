package com.ggs.service;

import com.ggs.exception.BusinessException;
import com.ggs.interceptor.JwtInterceptor;
import com.ggs.mapper.ClazzMapper;
import com.ggs.mapper.CourseArrangeMapper;
import com.ggs.mapper.CourseMapper;
import com.ggs.mapper.CourseSelectionMapper;
import com.ggs.mapper.SemesterMapper;
import com.ggs.mapper.StudentMapper;
import com.ggs.pojo.Clazz;
import com.ggs.pojo.Course;
import com.ggs.pojo.CourseArrange;
import com.ggs.pojo.CourseSelection;
import com.ggs.pojo.Semester;
import com.ggs.pojo.Student;
import com.ggs.util.DataScopeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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

    @Autowired
    private SemesterMapper semesterMapper;

    @Autowired
    private StatusChangeService statusChangeService;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private SelectionQuotaService quotaService;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private ClazzMapper clazzMapper;

    /** 数据范围校验：学生所属班级院系必须在本人范围内 */
    private void checkScopeBySid(Integer sid) {
        if (sid == null) {
            return;
        }
        Student student = studentMapper.getById(sid);
        if (student == null) {
            return;
        }
        Clazz clazz = clazzMapper.getById(student.getCid());
        DataScopeUtil.requireInScope(clazz == null ? null : clazz.getDid());
    }

    @Override
    public List<CourseSelection> listAll() {
        return selectionMapper.listAll(DataScopeUtil.currentDid());
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
        checkScopeBySid(sid);
        if (selectionMapper.countStudent(sid) == 0) {
            throw new BusinessException("学生不存在");
        }
        // 学籍状态拦截：休学/退学学生暂缓选课
        String blocked = statusChangeService.blockedStatus(sid);
        if (blocked != null) {
            throw new BusinessException("该学生当前学籍状态为【" + blocked + "】，无法选课");
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
        // 默认挂当前学期（无当前学期则 NULL，兼容旧数据）
        Semester current = semesterMapper.getCurrent();
        selection.setSemesterId(current == null ? null : current.getId());
        Long uid = JwtInterceptor.UID_HOLDER.get();
        selection.setUid(uid == null ? null : uid.intValue());
        selection.setCreateTime(LocalDateTime.now());

        // 容量配额：占用名额（Redis 原子扣减 / DB 条件扣减降级）
        Course course = courseMapper.getById(courseId);
        int capacity = course == null || course.getCapacity() == null ? 0 : course.getCapacity();
        SelectionQuotaService.QuotaResult quota =
                quotaService.tryOccupy(sid, courseId, selection.getSemesterId(), capacity);
        if (!quota.ok) {
            throw new BusinessException(quota.message);
        }
        try {
            selectionMapper.insert(selection);
        } catch (DuplicateKeyException e) {
            // 唯一索引兜底：并发下重复选课，回补名额
            quotaService.release(sid, courseId, selection.getSemesterId());
            throw new BusinessException("该学生已选此课程，请勿重复选课");
        }
    }

    @Override
    public boolean hasSelected(Integer sid, Integer courseId) {
        if (sid == null || courseId == null) {
            return false;
        }
        return selectionMapper.countBySidAndCourse(sid, courseId) > 0;
    }

    @Override
    public void delete(Integer id) {        if (id == null) {
            throw new BusinessException("删除失败，选课记录不存在");
        }
        CourseSelection exist = selectionMapper.getById(id);
        if (exist == null) {
            throw new BusinessException("删除失败，选课记录不存在");
        }
        checkScopeBySid(exist.getSid());
        if (selectionMapper.delete(id) == 0) {
            throw new BusinessException("删除失败，选课记录不存在");
        }
        // 退选回补名额
        quotaService.release(exist.getSid(), exist.getCourseId(), exist.getSemesterId());
    }

    @Override
    public List<Map<String, Object>> listCourseOptions(Integer sid) {
        return selectionMapper.listCourseOptions(sid);
    }

    @Override
    public List<Map<String, Object>> listScheduleSlots(Integer sid, Integer semesterId) {
        return selectionMapper.listScheduleSlots(sid, semesterId);
    }
}