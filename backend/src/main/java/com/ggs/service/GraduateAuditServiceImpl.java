package com.ggs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ggs.exception.BusinessException;
import com.ggs.mapper.GraduateAuditMapper;
import com.ggs.mapper.SemesterMapper;
import com.ggs.mapper.StudentMapper;
import com.ggs.mapper.TrainingPlanMapper;
import com.ggs.pojo.GraduateAudit;
import com.ggs.pojo.PlanCourse;
import com.ggs.pojo.Score;
import com.ggs.pojo.Semester;
import com.ggs.pojo.Student;
import com.ggs.pojo.TrainingPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GraduateAuditServiceImpl implements GraduateAuditService {

    /** 无培养方案时的默认总学分要求 */
    public static final double DEFAULT_TOTAL_CREDIT = 150.0;
    /** 默认绩点要求 */
    public static final double DEFAULT_GPA = 2.0;

    @Autowired
    private GraduateAuditMapper auditMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private SemesterMapper semesterMapper;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private TrainingPlanMapper planMapper;

    @Autowired
    private StatusChangeService statusChangeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Map<String, Object> audit(Integer sid, Double requiredTotalCredit, Double requiredGpa, Integer auditorUid) {
        if (sid == null) {
            throw new BusinessException("请选择要审核的学生");
        }
        Student student = studentMapper.getById(sid);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        // （批次I）学籍状态拦截：休学/退学学生不参与毕业审核
        String blockedStatus = blockedEnrollmentStatus(sid);
        if (blockedStatus != null) {
            throw new BusinessException("该学生当前学籍状态为【" + blockedStatus + "】，无法进行毕业审核");
        }

        double requiredCredit = requiredTotalCredit == null ? DEFAULT_TOTAL_CREDIT : requiredTotalCredit;
        double requireGpa = requiredGpa == null ? DEFAULT_GPA : requiredGpa;
        if (requiredCredit < 0 || requireGpa < 0) {
            throw new BusinessException("学分与绩点要求不能为负数");
        }

        // （批次I）有培养方案时按方案校验，无方案回退总学分模式
        Map<String, Object> detail = auditByPlanOrFallback(student, requiredCredit, requireGpa);

        // 结论留档
        Semester current = semesterMapper.getCurrent();
        GraduateAudit record = new GraduateAudit();
        record.setSid(sid);
        record.setSemesterId(current == null ? null : current.getId());
        record.setConclusion((String) detail.get("conclusion"));
        record.setDetailJson(writeJson(detail));
        record.setAuditorUid(auditorUid);
        record.setAuditTime(LocalDateTime.now());
        auditMapper.insert(record);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", record.getId());
        result.put("sid", sid);
        result.put("studentName", student.getName());
        result.put("semesterName", current == null ? null : current.getName());
        result.put("conclusion", record.getConclusion());
        result.put("auditTime", record.getAuditTime());
        result.put("detail", detail);
        return result;
    }

    /**
     * 毕业审核核心判定：
     * - 学生班级有培养方案 → 按方案校验（必修全通过、必修/选修学分达标、总学分达标、绩点达标）；
     * - 无培养方案 → 回退总学分模式（总学分 + 已修课程全部通过 + 绩点达标）。
     */
    protected Map<String, Object> auditByPlanOrFallback(Student student, double requiredCredit, double requireGpa) {
        Map<String, Object> core = scoreService.myScores(student.getSid(), null);
        @SuppressWarnings("unchecked")
        List<Score> scores = (List<Score>) core.get("list");
        double gpa = ((Number) core.getOrDefault("gpa", 0.0)).doubleValue();

        TrainingPlan plan = student.getCid() == null ? null : planMapper.getByCid(student.getCid());
        if (plan != null) {
            return auditByPlan(plan, scores, gpa, requireGpa);
        }
        return auditFallback(scores, gpa, requiredCredit, requireGpa);
    }

    /** 培养方案模式（批次I） */
    private Map<String, Object> auditByPlan(TrainingPlan plan,
                                            List<Score> scores, double gpa, double requireGpa) {
        List<PlanCourse> planCourses = planMapper.listCourses(plan.getId());
        Map<Integer, Score> scoreByCourse = new LinkedHashMap<>();
        for (Score s : scores) {
            scoreByCourse.putIfAbsent(s.getCourseId(), s);
        }
        List<String> missingRequired = new ArrayList<>();
        List<String> failedRequired = new ArrayList<>();
        double requiredCreditEarned = 0.0;
        double electiveCreditEarned = 0.0;
        double earned = 0.0;
        List<Map<String, Object>> failedAll = new ArrayList<>();

        for (Score s : scores) {
            Double credit = s.getCredit() == null ? 0.0 : s.getCredit().doubleValue();
            if (s.getTotal() == null) {
                continue;
            }
            if (s.getTotal() >= 60) {
                earned += credit;
            } else {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("courseName", s.getCourseName());
                item.put("total", s.getTotal());
                failedAll.add(item);
            }
        }

        for (PlanCourse pc : planCourses) {
            Score s = scoreByCourse.get(pc.getCourseId());
            String courseName = pc.getCourseName() == null ? "课程" + pc.getCourseId() : pc.getCourseName();
            double planCredit = pc.getRequireCredit() == null ? 0.0 : pc.getRequireCredit();
            if ("必修".equals(pc.getType())) {
                if (s == null) {
                    missingRequired.add(courseName);
                } else if (s.getTotal() == null || s.getTotal() < 60) {
                    failedRequired.add(courseName);
                } else {
                    requiredCreditEarned += planCredit;
                }
            } else {
                if (s != null && s.getTotal() != null && s.getTotal() >= 60) {
                    electiveCreditEarned += planCredit;
                }
            }
        }

        boolean creditOk = earned >= nvl(plan.getTotalCreditRequired());
        boolean requiredOk = missingRequired.isEmpty() && failedRequired.isEmpty();
        boolean electiveOk = electiveCreditEarned >= nvl(plan.getElectiveCreditRequired());
        boolean gpaOk = gpa >= requireGpa;
        boolean pass = creditOk && requiredOk && electiveOk && gpaOk;

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("mode", "plan");
        detail.put("planName", plan.getName() == null ? plan.getClazzName() : plan.getName());
        detail.put("requiredOk", requiredOk);
        detail.put("requiredCreditEarned", round1(requiredCreditEarned));
        detail.put("requiredCreditRequired", nvl(plan.getRequiredCreditRequired()));
        detail.put("missingRequired", missingRequired);
        detail.put("failedRequired", failedRequired);
        detail.put("electiveOk", electiveOk);
        detail.put("electiveCreditEarned", round1(electiveCreditEarned));
        detail.put("electiveCreditRequired", nvl(plan.getElectiveCreditRequired()));
        detail.put("earnedCredit", round1(earned));
        detail.put("requiredCredit", nvl(plan.getTotalCreditRequired()));
        detail.put("creditOk", creditOk);
        detail.put("gpa", round2(gpa));
        detail.put("requiredGpa", requireGpa);
        detail.put("gpaOk", gpaOk);
        detail.put("failedCourses", failedAll);
        detail.put("conclusion", pass ? "通过" : "不通过");
        return detail;
    }

    /** 总学分模式（无培养方案回退） */
    private Map<String, Object> auditFallback(List<Score> scores, double gpa, double requiredCredit, double requireGpa) {
        double earned = 0.0;
        List<Map<String, Object>> failed = new ArrayList<>();
        for (Score s : scores) {
            Double credit = s.getCredit() == null ? 0.0 : s.getCredit().doubleValue();
            if (s.getTotal() == null) {
                continue;
            }
            if (s.getTotal() >= 60) {
                earned += credit;
            } else {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("courseName", s.getCourseName());
                item.put("total", s.getTotal());
                failed.add(item);
            }
        }
        boolean creditOk = earned >= requiredCredit;
        boolean passedOk = failed.isEmpty();
        boolean gpaOk = gpa >= requireGpa;
        boolean pass = creditOk && passedOk && gpaOk;

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("mode", "fallback");
        detail.put("earnedCredit", round1(earned));
        detail.put("requiredCredit", requiredCredit);
        detail.put("creditOk", creditOk);
        detail.put("passedOk", passedOk);
        detail.put("failedCourses", failed);
        detail.put("gpa", round2(gpa));
        detail.put("requiredGpa", requireGpa);
        detail.put("gpaOk", gpaOk);
        detail.put("conclusion", pass ? "通过" : "不通过");
        return detail;
    }

    /** 学生当前学籍状态是否阻断（休学/退学），由学籍异动模块推导 */
    protected String blockedEnrollmentStatus(Integer sid) {
        return statusChangeService.blockedStatus(sid);
    }

    private double nvl(Double v) {
        return v == null ? 0.0 : v;
    }

    @Override
    public List<Map<String, Object>> history(Integer sid) {
        List<com.ggs.pojo.GraduateAudit> list = auditMapper.listHistory(sid);
        List<Map<String, Object>> result = new ArrayList<>();
        for (com.ggs.pojo.GraduateAudit g : list) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", g.getId());
            row.put("sid", g.getSid());
            row.put("studentName", g.getStudentName());
            row.put("semesterId", g.getSemesterId());
            row.put("semesterName", g.getSemesterName());
            row.put("conclusion", g.getConclusion());
            row.put("auditorName", g.getAuditorName());
            row.put("auditTime", g.getAuditTime());
            row.put("detail", parseJson(g.getDetailJson()));
            result.add(row);
        }
        return result;
    }

    protected double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    protected double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    protected String writeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    protected Object parseJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            return json;
        }
    }
}