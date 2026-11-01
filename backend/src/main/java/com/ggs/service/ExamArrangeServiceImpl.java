package com.ggs.service;

import com.ggs.exception.BusinessException;
import com.ggs.mapper.CourseMapper;
import com.ggs.mapper.ExamArrangeMapper;
import com.ggs.mapper.SemesterMapper;
import com.ggs.mapper.TeacherMapper;
import com.ggs.pojo.ExamArrange;
import com.ggs.pojo.Semester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ExamArrangeServiceImpl implements ExamArrangeService {

    @Autowired
    private ExamArrangeMapper examMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private SemesterMapper semesterMapper;

    @Override
    public List<ExamArrange> listAll() {
        return examMapper.listAll();
    }

    @Override
    public void add(ExamArrange exam) {
        validateAndNormalize(exam, null);
        examMapper.insert(exam);
    }

    @Override
    public void update(ExamArrange exam) {
        if (exam.getId() == null) {
            throw new BusinessException("参数错误：缺少考试安排ID");
        }
        validateAndNormalize(exam, exam.getId());
        examMapper.update(exam);
    }

    /** 通用校验与冲突检测；学期哨兵：null=默认当前学期，0=显式未分学期（NULL） */
    private void validateAndNormalize(ExamArrange exam, Integer excludeId) {
        if (exam.getCourseId() == null || courseMapper.getById(exam.getCourseId()) == null) {
            throw new BusinessException("请选择有效课程");
        }
        if (exam.getExamDate() == null) {
            throw new BusinessException("请选择考试日期");
        }
        Integer start = exam.getStartSection();
        Integer end = exam.getEndSection();
        if (start == null || end == null || start < 1 || end > 12 || start > end) {
            throw new BusinessException("节次不合法：开始节次 1-12 且不能大于结束节次");
        }
        if (exam.getRoom() == null || exam.getRoom().trim().isEmpty()) {
            throw new BusinessException("请填写考场");
        }
        exam.setRoom(exam.getRoom().trim());
        String type = exam.getType() == null ? "" : exam.getType().trim();
        if (type.isEmpty()) {
            exam.setType("期末");
        } else if (!List.of("期末", "补考", "重修").contains(type)) {
            throw new BusinessException("考试类型仅支持：期末/补考/重修");
        } else {
            exam.setType(type);
        }
        if (exam.getInvigilatorUid() != null && teacherMapper.getTeacherById(exam.getInvigilatorUid().longValue()) == null) {
            throw new BusinessException("监考教师不存在");
        }
        // 学期默认逻辑（与成绩/选课模块一致）
        if (exam.getSemesterId() == null) {
            Semester current = semesterMapper.getCurrent();
            exam.setSemesterId(current == null ? null : current.getId());
        } else if (exam.getSemesterId() == 0) {
            exam.setSemesterId(null);
        }

        // 冲突校验一：同课程同学期不重复排
        if (examMapper.countByCourseSemester(excludeId, exam.getCourseId(), exam.getSemesterId()) > 0) {
            throw new BusinessException("该课程本学期已安排考试，请勿重复排考");
        }
        // 冲突校验二：同考场同日期时段不重叠
        List<Map<String, Object>> roomConflicts = examMapper.listRoomConflicts(
                excludeId, exam.getRoom(), exam.getExamDate(), start, end);
        if (!roomConflicts.isEmpty()) {
            Map<String, Object> row = roomConflicts.get(0);
            throw new BusinessException("考场冲突：「" + exam.getRoom() + "」在 " + exam.getExamDate()
                    + " 第" + row.get("startSection") + "-" + row.get("endSection") + "节已安排考试（"
                    + row.get("courseName") + "）");
        }
        // 冲突校验三：同监考教师同日期时段不重叠
        if (exam.getInvigilatorUid() != null) {
            List<Map<String, Object>> teacherConflicts = examMapper.listTeacherConflicts(
                    excludeId, exam.getInvigilatorUid(), exam.getExamDate(), start, end);
            if (!teacherConflicts.isEmpty()) {
                Map<String, Object> row = teacherConflicts.get(0);
                String teacherName = teacherMapper.getTeacherById(exam.getInvigilatorUid().longValue()).getName();
                throw new BusinessException("监考冲突：教师「" + teacherName + "」在 " + exam.getExamDate()
                        + " 第" + row.get("startSection") + "-" + row.get("endSection") + "节已有监考安排（"
                        + row.get("courseName") + "，" + row.get("room") + "）");
            }
        }
    }

    @Override
    public void delete(Integer id) {
        if (id == null || examMapper.delete(id) == 0) {
            throw new BusinessException("删除失败，考试安排不存在");
        }
    }

    @Override
    public List<Map<String, Object>> myExams(Integer sid) {
        return examMapper.listMyExams(sid);
    }
}