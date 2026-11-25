package com.ggs.service;

import com.ggs.exception.BusinessException;
import com.ggs.mapper.SelectionRoundMapper;
import com.ggs.mapper.SelectionWishMapper;
import com.ggs.mapper.SemesterMapper;
import com.ggs.pojo.SelectionRound;
import com.ggs.pojo.SelectionWish;
import com.ggs.pojo.Semester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SelectionRoundServiceImpl implements SelectionRoundService {

    @Autowired
    private SelectionRoundMapper roundMapper;

    @Autowired
    private SelectionWishMapper wishMapper;

    @Autowired
    private SemesterMapper semesterMapper;

    @Autowired
    private CourseSelectionService selectionService;

    @Autowired
    private MessageService messageService;

    @Override
    public List<SelectionRound> list(Integer semesterId) {
        return roundMapper.list(resolveSemester(semesterId));
    }

    @Override
    public void create(SelectionRound round) {
        if (round.getName() == null || round.getName().trim().isEmpty()) {
            throw new BusinessException("请填写轮次名称");
        }
        if (round.getType() == null || (round.getType() != 1 && round.getType() != 2)) {
            throw new BusinessException("轮次类型仅支持：1初选/2补退选");
        }
        if (round.getMaxWishes() == null || round.getMaxWishes() < 1 || round.getMaxWishes() > 10) {
            round.setMaxWishes(3);
        }
        if (round.getSemesterId() == null) {
            Semester current = semesterMapper.getCurrent();
            round.setSemesterId(current == null ? 0 : current.getId());
        }
        round.setStatus(0);
        roundMapper.insert(round);
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        SelectionRound round = roundMapper.getById(id);
        if (round == null) {
            throw new BusinessException("轮次不存在");
        }
        if (status == null || (status != 1 && status != 2)) {
            throw new BusinessException("目标状态仅支持：1进行中/2已结束");
        }
        if (round.getStatus() == null || round.getStatus() != status - 1) {
            throw new BusinessException("仅支持按 未开始->进行中->已结束 顺序流转");
        }
        if (status == 1 && roundMapper.countRunningByType(round.getType(), round.getSemesterId()) > 0) {
            throw new BusinessException("同类型已存在进行中的轮次");
        }
        roundMapper.updateStatus(id, status);
    }

    @Override
    public Map<String, Object> current() {
        Map<String, Object> map = new HashMap<>();
        map.put("first", roundMapper.findRunning(1));
        map.put("second", roundMapper.findRunning(2));
        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitWishes(Integer sid, Integer roundId, List<Integer> courseIds) {
        SelectionRound round = roundMapper.getById(roundId);
        if (round == null) {
            throw new BusinessException("选课轮次不存在");
        }
        if (round.getType() == null || round.getType() != 1) {
            throw new BusinessException("仅初选轮次支持提交志愿");
        }
        if (round.getStatus() == null || round.getStatus() != 1) {
            throw new BusinessException("该轮次不在进行中，无法提交志愿");
        }
        if (courseIds == null || courseIds.isEmpty()) {
            throw new BusinessException("请至少选择一门志愿课程");
        }
        // 去重且保持顺序
        List<Integer> distinct = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        for (Integer cid : courseIds) {
            if (cid != null && seen.add(cid)) {
                distinct.add(cid);
            }
        }
        if (distinct.size() > round.getMaxWishes()) {
            throw new BusinessException("志愿数量超过上限（最多 " + round.getMaxWishes() + " 门）");
        }
        wishMapper.deleteByRoundAndSid(roundId, sid);
        for (int i = 0; i < distinct.size(); i++) {
            SelectionWish wish = new SelectionWish();
            wish.setRoundId(roundId);
            wish.setSid(sid);
            wish.setCourseId(distinct.get(i));
            wish.setPriority(i + 1);
            wishMapper.insert(wish);
        }
    }

    @Override
    public List<SelectionWish> listWishes(Integer sid, Integer roundId) {
        return wishMapper.listByRoundAndSid(roundId, sid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> allocate(Integer roundId) {
        SelectionRound round = roundMapper.getById(roundId);
        if (round == null) {
            throw new BusinessException("选课轮次不存在");
        }
        if (round.getType() == null || round.getType() != 1) {
            throw new BusinessException("仅初选轮次支持投档");
        }
        if (round.getStatus() == null || round.getStatus() != 1) {
            throw new BusinessException("仅进行中的初选轮次可投档");
        }

        List<SelectionWish> wishes = wishMapper.listByRound(roundId);
        // 按优先级分组（保持提交时间顺序）
        Map<Integer, List<SelectionWish>> byPriority = new LinkedHashMap<>();
        for (SelectionWish wish : wishes) {
            byPriority.computeIfAbsent(wish.getPriority(), k -> new ArrayList<>()).add(wish);
        }

        Set<Integer> admittedStudents = new HashSet<>();
        Map<Integer, List<String>> admittedCourses = new HashMap<>(); // sid -> 课程名
        Set<Integer> wishStudents = new HashSet<>();
        for (SelectionWish w : wishes) {
            wishStudents.add(w.getSid());
        }

        for (Map.Entry<Integer, List<SelectionWish>> entry : byPriority.entrySet()) {
            // 当前优先级下按课程分组，学生按提交时间先后
            Map<Integer, List<SelectionWish>> byCourse = new LinkedHashMap<>();
            for (SelectionWish w : entry.getValue()) {
                byCourse.computeIfAbsent(w.getCourseId(), k -> new ArrayList<>()).add(w);
            }
            for (List<SelectionWish> courseWishes : byCourse.values()) {
                for (SelectionWish w : courseWishes) {
                    if (admittedStudents.contains(w.getSid())) {
                        continue;
                    }
                    try {
                        selectionService.add(w.getSid(), w.getCourseId());
                        admittedStudents.add(w.getSid());
                        admittedCourses.computeIfAbsent(w.getSid(), k -> new ArrayList<>())
                                .add(w.getCourseName() == null ? String.valueOf(w.getCourseId()) : w.getCourseName());
                    } catch (BusinessException e) {
                        // 名额已满/时间冲突：顺延下一志愿
                    }
                }
            }
        }

        roundMapper.updateStatus(roundId, 2);

        // 消息通知：录取/未录取
        for (Integer sid : wishStudents) {
            List<String> courses = admittedCourses.get(sid);
            if (courses == null || courses.isEmpty()) {
                messageService.send("student", sid.longValue(), "selection",
                        "选课投档结果：未录取",
                        "很遗憾，你在「" + round.getName() + "」提交的志愿均未录取，请在补退选阶段选课", (long) roundId);
            } else {
                messageService.send("student", sid.longValue(), "selection",
                        "选课投档结果：已录取",
                        "你在「" + round.getName() + "」中被录取课程：" + String.join("、", courses), (long) roundId);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", wishStudents.size());
        result.put("admitted", admittedStudents.size());
        result.put("failed", wishStudents.size() - admittedStudents.size());
        return result;
    }

    private Integer resolveSemester(Integer semesterId) {
        if (semesterId != null) {
            return semesterId;
        }
        Semester current = semesterMapper.getCurrent();
        return current == null ? 0 : current.getId();
    }
}
