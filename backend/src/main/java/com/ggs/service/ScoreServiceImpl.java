package com.ggs.service;

import com.ggs.exception.BusinessException;
import com.ggs.mapper.ScoreMapper;
import com.ggs.mapper.SemesterMapper;
import com.ggs.pojo.Score;
import com.ggs.pojo.Semester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScoreServiceImpl implements ScoreService {

    @Autowired
    private ScoreMapper scoreMapper;

    @Autowired
    private SemesterMapper semesterMapper;

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
        validateScoreRange(score);
        applyDefaultSemester(score);
        validateDuplicate(score.getSid(), score.getCourseId(), score.getSemesterId(), null);
        calculateTotalAndGp(score);
        return scoreMapper.add(score);
    }

    @Override
    public int update(Score score) {
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
}
