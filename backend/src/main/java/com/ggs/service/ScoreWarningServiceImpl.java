package com.ggs.service;

import com.ggs.mapper.ScoreMapper;
import com.ggs.pojo.Score;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScoreWarningServiceImpl implements ScoreWarningService {

    @Autowired
    private ScoreMapper scoreMapper;

    @Override
    public List<Map<String, Object>> failScores() {
        List<Score> all = scoreMapper.listScore();
        return all.stream()
                .filter(s -> s.getTotal() != null && s.getTotal() < 60)
                .map(s -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("sid", s.getSid());
                    map.put("studentName", s.getStudentName());
                    map.put("clazzName", s.getClazzName());
                    map.put("courseName", s.getCourseName());
                    map.put("total", s.getTotal());
                    map.put("type", "不及格");
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> lowGpaStudents() {
        List<Score> all = scoreMapper.listScore();
        Map<Integer, List<Score>> grouped = all.stream()
                .filter(s -> s.getGp() != null)
                .collect(Collectors.groupingBy(Score::getSid));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Integer, List<Score>> entry : grouped.entrySet()) {
            List<Score> scores = entry.getValue();
            double avgGpa = scores.stream().mapToDouble(Score::getGp).average().orElse(0.0);
            if (avgGpa < 2.0) {
                Score first = scores.get(0);
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("sid", entry.getKey());
                map.put("studentName", first.getStudentName());
                map.put("clazzName", first.getClazzName());
                map.put("gpa", Math.round(avgGpa * 10.0) / 10.0);
                map.put("courseCount", scores.size());
                map.put("type", "低绩点");
                result.add(map);
            }
        }
        result.sort(Comparator.comparingDouble(m -> (double) m.get("gpa")));
        return result;
    }
}
