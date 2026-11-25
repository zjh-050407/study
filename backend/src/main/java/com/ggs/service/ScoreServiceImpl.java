package com.ggs.service;

import com.ggs.exception.BusinessException;
import com.ggs.mapper.ClazzMapper;
import com.ggs.mapper.ScoreAppealMapper;
import com.ggs.mapper.ScoreMapper;
import com.ggs.mapper.SemesterMapper;
import com.ggs.mapper.StudentMapper;
import com.ggs.pojo.Clazz;
import com.ggs.pojo.Score;
import com.ggs.pojo.ScoreAppeal;
import com.ggs.pojo.Semester;
import com.ggs.pojo.Student;
import com.ggs.util.DataScopeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScoreServiceImpl implements ScoreService {

    /** 成绩状态：0草稿/1待审核/2已发布/3复核中 */
    private static final int ST_DRAFT = 0;
    private static final int ST_SUBMITTED = 1;
    private static final int ST_PUBLISHED = 2;
    private static final int ST_APPEALED = 3;

    @Autowired
    private ScoreMapper scoreMapper;

    @Autowired
    private SemesterMapper semesterMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private ClazzMapper clazzMapper;

    @Autowired
    private ScoreAppealMapper appealMapper;

    @Autowired
    private MessageService messageService;

    /** 数据范围校验：成绩所属学生班级院系必须在本人范围内 */
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
    public List<Score> listScore() {
        return scoreMapper.listScore();
    }

    @Override
    public List<Score> listByStudent(Integer sid) {
        return scoreMapper.listByStudent(sid);
    }

    @Override
    public List<Score> listByCourse(Integer courseId) {
        return scoreMapper.listByCourse(courseId);
    }

    @Override
    public int add(Score score) {
        checkScopeBySid(score.getSid());
        validateScoreRange(score);
        applyDefaultSemester(score);
        validateDuplicate(score.getSid(), score.getCourseId(), score.getSemesterId(), null);
        calculateTotalAndGp(score);
        return scoreMapper.add(score);
    }

    @Override
    public int update(Score score) {
        checkScopeBySid(score.getSid());
        // 已发布成绩不可直接修改，需走复核流程（批次N）
        Score existing = scoreMapper.getById(score.getScoreId());
        if (existing != null && existing.getStatus() != null && existing.getStatus() == ST_PUBLISHED) {
            throw new BusinessException("已发布成绩不可直接修改，请通过复核流程调整");
        }
        validateScoreRange(score);
        applyDefaultSemester(score);
        validateDuplicate(score.getSid(), score.getCourseId(), score.getSemesterId(), score.getScoreId());
        calculateTotalAndGp(score);
        return scoreMapper.update(score);
    }

    /** 学期默认逻辑：null=默认挂当前学期；0=显式未分学期（NULL） */
    private void applyDefaultSemester(Score score) {
        if (score.getSemesterId() == null) {
            Semester current = semesterMapper.getCurrent();
            score.setSemesterId(current == null ? null : current.getId());
        } else if (score.getSemesterId() == 0) {
            score.setSemesterId(null);
        }
    }

    private void validateScoreRange(Score score) {
        double usual = score.getUsual() == null ? 0.0 : score.getUsual();
        double mid = score.getMid() == null ? 0.0 : score.getMid();
        double finalScore = score.getFinalScore() == null ? 0.0 : score.getFinalScore();
        if (usual < 0 || usual > 100 || mid < 0 || mid > 100 || finalScore < 0 || finalScore > 100) {
            throw new BusinessException("成绩必须在0-100之间");
        }
    }

    private void validateDuplicate(Integer sid, Integer courseId, Integer semesterId, Integer excludeId) {
        if (sid == null || courseId == null) {
            throw new BusinessException("学生与课程不能为空");
        }
        if (scoreMapper.countByStudentAndCourse(sid, courseId, semesterId, excludeId) > 0) {
            throw new BusinessException("该学生本学期该课程已有成绩，请勿重复录入");
        }
    }

    private void calculateTotalAndGp(Score score) {
        if (score.getUsual() == null) score.setUsual(0.0);
        if (score.getMid() == null) score.setMid(0.0);
        if (score.getFinalScore() == null) score.setFinalScore(0.0);
        double total = score.getUsual() * 0.3 + score.getMid() * 0.2 + score.getFinalScore() * 0.5;
        score.setTotal(Math.round(total * 10.0) / 10.0);
        score.setGp(calculateGp(score.getTotal()));
    }

    private double calculateGp(double total) {
        if (total >= 90) return 4.0;
        if (total >= 85) return 3.7;
        if (total >= 82) return 3.3;
        if (total >= 78) return 3.0;
        if (total >= 75) return 2.7;
        if (total >= 72) return 2.3;
        if (total >= 68) return 2.0;
        if (total >= 64) return 1.5;
        if (total >= 60) return 1.0;
        return 0.0;
    }

    @Override
    public int delete(Integer scoreId) {
        return scoreMapper.delete(scoreId);
    }

    @Override
    public List<Map<String, Object>> avgByCourse() {
        return scoreMapper.avgByCourse();
    }

    @Override
    public List<Map<String, Object>> countByGrade() {
        return scoreMapper.countByGrade();
    }

    @Override
    public Map<String, Object> myScores(Integer sid, Integer semesterId) {
        List<Score> list = scoreMapper.listByStudentAndSemester(sid, semesterId);
        double gpaSum = 0.0;
        double creditForGpa = 0.0;
        double totalCredit = 0.0;
        for (Score s : list) {
            double credit = s.getCredit() == null ? 0.0 : s.getCredit();
            totalCredit += credit;
            if (s.getGp() != null) {
                gpaSum += s.getGp() * credit;
                creditForGpa += credit;
            }
        }
        double gpa = creditForGpa > 0 ? Math.round(gpaSum / creditForGpa * 100.0) / 100.0 : 0.0;
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("gpa", gpa);
        result.put("totalCredit", totalCredit);
        return result;
    }

    /* ==================== 批次N：成绩流程 ==================== */

    @Override
    public int submit(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要提交的成绩");
        }
        return scoreMapper.updateStatusBatch(ids, ST_DRAFT, ST_SUBMITTED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int publish(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要发布的成绩");
        }
        int count = 0;
        for (Integer id : ids) {
            Score score = scoreMapper.getById(id);
            if (score == null || score.getStatus() == null || score.getStatus() != ST_SUBMITTED) {
                continue;
            }
            if (scoreMapper.updateStatus(id, ST_SUBMITTED, ST_PUBLISHED) == 1) {
                count++;
                messageService.send("student", score.getSid().longValue(), "score",
                        "成绩已发布", "你的《" + (score.getCourseName() == null ? "课程" : score.getCourseName()) + "》成绩已发布，可登录学生端查看", (long) id);
            }
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void appeal(Integer scoreId, Integer sid, String reason) {
        Score score = scoreMapper.getById(scoreId);
        if (score == null) {
            throw new BusinessException("成绩记录不存在");
        }
        if (score.getStatus() == null || score.getStatus() != ST_PUBLISHED) {
            throw new BusinessException("仅已发布的成绩可以申请复核");
        }
        if (!score.getSid().equals(sid)) {
            throw new BusinessException("只能对本人成绩发起复核");
        }
        if (appealMapper.countPendingByScore(scoreId) > 0) {
            throw new BusinessException("该成绩已有待处理的复核申请，请勿重复提交");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("请填写申诉理由");
        }
        ScoreAppeal appeal = new ScoreAppeal();
        appeal.setScoreId(scoreId);
        appeal.setSid(sid);
        appeal.setReason(reason.trim());
        appealMapper.insert(appeal);
        scoreMapper.updateStatus(scoreId, ST_PUBLISHED, ST_APPEALED);
        messageService.sendToRole("admin", "approval", "新的成绩复核申请",
                "学生学号 " + sid + " 对《" + (score.getCourseName() == null ? "课程" : score.getCourseName()) + "》成绩发起复核申请", appeal.getId().longValue());
        messageService.sendToRole("professor", "approval", "新的成绩复核申请",
                "学生学号 " + sid + " 对《" + (score.getCourseName() == null ? "课程" : score.getCourseName()) + "》成绩发起复核申请", appeal.getId().longValue());
    }

    @Override
    public List<ScoreAppeal> listAppealPending() {
        return appealMapper.listPending();
    }

    @Override
    public List<ScoreAppeal> listAppealByStudent(Integer sid) {
        return appealMapper.listByStudent(sid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAppeal(Integer appealId, String action, Double newTotal, String handleNote, Long handleUid) {
        ScoreAppeal appeal = appealMapper.getById(appealId);
        if (appeal == null) {
            throw new BusinessException("申诉单不存在");
        }
        if (appeal.getStatus() == null || appeal.getStatus() != 0) {
            throw new BusinessException("该申诉已处理，请勿重复操作");
        }
        Score score = scoreMapper.getById(appeal.getScoreId());
        if (score == null) {
            throw new BusinessException("成绩记录不存在");
        }
        boolean approve = "approve".equals(action);
        if (approve) {
            if (newTotal != null) {
                if (newTotal < 0 || newTotal > 100) {
                    throw new BusinessException("新总评必须在0-100之间");
                }
                double rounded = Math.round(newTotal * 10.0) / 10.0;
                scoreMapper.updateTotal(appeal.getScoreId(), rounded, calculateGp(rounded));
            }
            scoreMapper.updateStatus(appeal.getScoreId(), ST_APPEALED, ST_PUBLISHED);
            appealMapper.updateStatus(appealId, 1, newTotal, handleNote, handleUid, LocalDateTime.now());
            messageService.send("student", appeal.getSid().longValue(), "score",
                    "成绩复核已通过", "你申请的《" + (score.getCourseName() == null ? "课程" : score.getCourseName()) + "》成绩复核已通过" + (newTotal != null ? "，成绩已更新" : ""), appeal.getId().longValue());
        } else {
            scoreMapper.updateStatus(appeal.getScoreId(), ST_APPEALED, ST_PUBLISHED);
            appealMapper.updateStatus(appealId, 2, null, handleNote, handleUid, LocalDateTime.now());
            messageService.send("student", appeal.getSid().longValue(), "score",
                    "成绩复核被驳回", "你申请的《" + (score.getCourseName() == null ? "课程" : score.getCourseName()) + "》成绩复核未通过" + (handleNote == null || handleNote.isEmpty() ? "" : "：" + handleNote), appeal.getId().longValue());
        }
    }
}
