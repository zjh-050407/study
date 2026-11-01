package com.ggs.service;

import com.ggs.exception.BusinessException;
import com.ggs.mapper.EvaluationRecordMapper;
import com.ggs.mapper.EvaluationTaskMapper;
import com.ggs.mapper.SemesterMapper;
import com.ggs.pojo.EvaluationRecord;
import com.ggs.pojo.EvaluationTask;
import com.ggs.pojo.Semester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EvaluationServiceImpl implements EvaluationService {

    /** 匿名令牌混淆串：仅用于让 stu_token 无法被反推，不参与身份存储 */
    private static final String TOKEN_SALT = "ggs-evaluation-anonymous";

    @Autowired
    private EvaluationTaskMapper taskMapper;

    @Autowired
    private EvaluationRecordMapper recordMapper;

    @Autowired
    private SemesterMapper semesterMapper;

    @Override
    public void createTask(EvaluationTask task) {
        LocalDateTime start = task.getStartTime();
        LocalDateTime end = task.getEndTime();
        if (start == null || end == null) {
            throw new BusinessException("请填写评教起止时间");
        }
        if (!end.isAfter(start)) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
        if (!end.isAfter(LocalDateTime.now())) {
            throw new BusinessException("结束时间必须晚于当前时间");
        }
        Semester semester = resolveSemester(task.getSemesterId());
        if (taskMapper.countActiveBySemester(semester.getId()) > 0) {
            throw new BusinessException("该学期已有进行中的评教任务，请先关闭后再发起");
        }
        task.setSemesterId(semester.getId());
        task.setStatus(1);
        task.setCreateTime(LocalDateTime.now());
        taskMapper.insert(task);
    }

    @Override
    public void closeTask(Integer id) {
        if (id == null) {
            throw new BusinessException("参数错误：缺少任务ID");
        }
        EvaluationTask task = taskMapper.getById(id);
        if (task == null) {
            throw new BusinessException("评教任务不存在");
        }
        if (task.getStatus() == null || task.getStatus() != 1) {
            throw new BusinessException("该任务已关闭，请勿重复操作");
        }
        taskMapper.updateStatus(id, 0);
    }

    @Override
    public List<EvaluationTask> listTasks() {
        return taskMapper.listAll();
    }

    @Override
    public Map<String, Object> activeWindow() {
        Semester current = semesterMapper.getCurrent();
        Map<String, Object> result = new HashMap<>();
        result.put("semesterName", current == null ? null : current.getName());
        if (current == null) {
            result.put("task", null);
            result.put("windowOpen", false);
            return result;
        }
        EvaluationTask task = taskMapper.getActiveBySemester(current.getId());
        result.put("task", task);
        boolean open = isWindowOpen(task);
        result.put("windowOpen", open);
        return result;
    }

    @Override
    public List<Map<String, Object>> listEligibleForStudent(Integer sid) {
        Semester current = semesterMapper.getCurrent();
        if (current == null) {
            return List.of();
        }
        EvaluationTask task = taskMapper.getActiveBySemester(current.getId());
        if (task == null) {
            return List.of();
        }
        List<Map<String, Object>> rows = recordMapper.listEligible(sid, current.getId());
        for (Map<String, Object> row : rows) {
            Integer courseId = (Integer) row.get("courseId");
            Integer teacherUid = (Integer) row.get("teacherUid");
            String token = buildToken(sid, task.getId(), courseId, teacherUid);
            row.put("evaluated", recordMapper.countByToken(task.getId(), courseId, teacherUid, token) > 0);
        }
        return rows;
    }

    @Override
    public void submit(Integer sid, EvaluationRecord record) {
        if (sid == null || record.getTaskId() == null || record.getCourseId() == null || record.getTeacherUid() == null) {
            throw new BusinessException("参数错误：缺少评教信息");
        }
        EvaluationTask task = taskMapper.getById(record.getTaskId());
        if (task == null) {
            throw new BusinessException("评教任务不存在");
        }
        if (task.getStatus() == null || task.getStatus() != 1 || !isWindowOpen(task)) {
            throw new BusinessException("评教未开启或已结束");
        }
        // 指定任务属于哪个学期，就只能评哪个学期的课程，防止跨学期乱评
        if (recordMapper.countSelectionInSemester(sid, record.getCourseId(), task.getSemesterId()) == 0) {
            throw new BusinessException("你本学期未选修该课程，无法评教");
        }
        if (recordMapper.countTeacherOfCourse(record.getCourseId(), record.getTeacherUid()) == 0) {
            throw new BusinessException("任课教师与课程不匹配");
        }
        checkDim(record.getDim1(), "教学态度");
        checkDim(record.getDim2(), "教学内容");
        checkDim(record.getDim3(), "教学方法");
        checkDim(record.getDim4(), "教学效果");
        String suggestion = record.getSuggestion();
        if (suggestion != null) {
            suggestion = suggestion.trim();
            if (suggestion.length() > 500) {
                throw new BusinessException("建议内容不能超过500字");
            }
            if (suggestion.isEmpty()) {
                suggestion = null;
            }
        }
        String token = buildToken(sid, task.getId(), record.getCourseId(), record.getTeacherUid());
        if (recordMapper.countByToken(task.getId(), record.getCourseId(), record.getTeacherUid(), token) > 0) {
            throw new BusinessException("该课程已评过，评教提交后不可修改");
        }
        record.setStuToken(token);
        record.setSuggestion(suggestion);
        record.setCreateTime(LocalDateTime.now());
        try {
            recordMapper.insert(record);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("该课程已评过，评教提交后不可修改");
        }
    }

    @Override
    public List<Map<String, Object>> stats(Integer taskId) {
        if (taskId == null) {
            throw new BusinessException("参数错误：缺少任务ID");
        }
        List<Map<String, Object>> list = recordMapper.listStats(taskId);
        for (Map<String, Object> row : list) {
            Integer courseId = (Integer) row.get("courseId");
            Integer teacherUid = (Integer) row.get("teacherUid");
            row.put("suggestions", recordMapper.listSuggestions(taskId, courseId, teacherUid));
        }
        return list;
    }

    @Override
    public String exportCsv(Integer taskId) {
        List<Map<String, Object>> stats = stats(taskId);
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF'); // BOM，Excel 打开中文不乱码
        sb.append("课程,任课教师,参评人数,教学态度均分,教学内容均分,教学方法均分,教学效果均分,综合均分,学生建议\n");
        for (Map<String, Object> row : stats) {
            List<?> suggestions = (List<?>) row.get("suggestions");
            String sug = suggestions == null || suggestions.isEmpty()
                    ? "" : String.join("；", suggestions.stream().map(String::valueOf).toList());
            sb.append(csvField(row.get("courseName"))).append(',')
              .append(csvField(row.get("teacherName"))).append(',')
              .append(row.get("recordCount")).append(',')
              .append(row.get("avgDim1")).append(',')
              .append(row.get("avgDim2")).append(',')
              .append(row.get("avgDim3")).append(',')
              .append(row.get("avgDim4")).append(',')
              .append(row.get("avgTotal")).append(',')
              .append(csvField(sug)).append('\n');
        }
        return sb.toString();
    }

    private String csvField(Object value) {
        String s = value == null ? "" : String.valueOf(value);
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private void checkDim(Integer dim, String name) {
        if (dim == null || dim < 1 || dim > 5) {
            throw new BusinessException(name + "评分须为1-5分");
        }
    }

    private boolean isWindowOpen(EvaluationTask task) {
        if (task == null || task.getStatus() == null || task.getStatus() != 1) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(task.getStartTime()) && !now.isAfter(task.getEndTime());
    }

    private Semester resolveSemester(Integer semesterId) {
        Semester semester = semesterId == null ? semesterMapper.getCurrent() : semesterMapper.getById(semesterId);
        if (semester == null) {
            throw new BusinessException("请先设置当前学期");
        }
        return semester;
    }

    /** 匿名令牌：SHA-256(sid:taskId:courseId:teacherUid:salt)，库中不落学号，仅用于防重复评 */
    private String buildToken(Integer sid, Integer taskId, Integer courseId, Integer teacherUid) {
        try {
            String raw = sid + ":" + taskId + ":" + courseId + ":" + teacherUid + ":" + TOKEN_SALT;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new BusinessException("系统异常，请稍后重试");
        }
    }
}