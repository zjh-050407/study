package com.ggs.service;

import com.ggs.exception.BusinessException;
import com.ggs.mapper.CourseMapper;
import com.ggs.mapper.CourseSelectionMapper;
import com.ggs.mapper.SelectionApplyMapper;
import com.ggs.pojo.Course;
import com.ggs.pojo.CourseSelection;
import com.ggs.pojo.SelectionApply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SelectionApplyServiceImpl implements SelectionApplyService {

    @Autowired
    private SelectionApplyMapper applyMapper;

    @Autowired
    private CourseSelectionMapper selectionMapper;

    @Autowired
    private SelectionQuotaService quotaService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private CourseMapper courseMapper;

    private String courseName(Integer courseId) {
        if (courseId == null) {
            return "未知课程";
        }
        Course course = courseMapper.getById(courseId);
        return course == null || course.getName() == null ? String.valueOf(courseId) : course.getName();
    }

    @Override
    public void apply(Integer sid, Integer courseId, String reason) {
        if (sid == null || courseId == null) {
            throw new BusinessException("参数错误：缺少学生或课程");
        }
        if (applyMapper.countSelection(sid, courseId) == 0) {
            throw new BusinessException("你未选修该课程，无法提交退选申请");
        }
        if (applyMapper.countPendingBySidAndCourse(sid, courseId) > 0) {
            throw new BusinessException("该课程的退选申请正在审批中，请勿重复提交");
        }
        SelectionApply apply = new SelectionApply();
        apply.setSid(sid);
        apply.setCourseId(courseId);
        apply.setReason(reason);
        apply.setStatus(0);
        apply.setApplyTime(LocalDateTime.now());
        applyMapper.insert(apply);
        // 消息埋点：通知管理员与教授有待审批退选申请
        String title = "新的退选申请待审批";
        String content = "学生学号 " + sid + " 申请退选《" + courseName(courseId) + "》";
        messageService.sendToRole("admin", "approval", title, content, apply.getId().longValue());
        messageService.sendToRole("professor", "approval", title, content, apply.getId().longValue());
    }

    @Override
    public List<SelectionApply> listByStudent(Integer sid) {
        return applyMapper.listByStudent(sid);
    }

    @Override
    public List<SelectionApply> listPending() {
        return applyMapper.listPending();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Integer id, Integer approveUid) {
        SelectionApply apply = applyMapper.getById(id);
        if (apply == null) {
            throw new BusinessException("申请单不存在");
        }
        if (apply.getStatus() == null || apply.getStatus() != 0) {
            throw new BusinessException("该申请已处理，请勿重复审批");
        }
        // 删除选课记录（若已被教师代选侧删除则忽略），并回补名额
        CourseSelection target = selectionMapper.listByStudent(apply.getSid()).stream()
                .filter(s -> s.getCourseId() != null && s.getCourseId().equals(apply.getCourseId()))
                .findFirst().orElse(null);
        selectionMapper.deleteBySidAndCourse(apply.getSid(), apply.getCourseId());
        if (target != null) {
            quotaService.release(target.getSid(), target.getCourseId(), target.getSemesterId());
        }
        applyMapper.updateStatus(id, 1, approveUid, LocalDateTime.now());
        messageService.send("student", apply.getSid().longValue(), "approval",
                "退选申请已通过", "你申请退选的《" + courseName(apply.getCourseId()) + "》已通过审批", apply.getId().longValue());
    }

    @Override
    public void reject(Integer id, Integer approveUid) {
        SelectionApply apply = applyMapper.getById(id);
        if (apply == null) {
            throw new BusinessException("申请单不存在");
        }
        if (apply.getStatus() == null || apply.getStatus() != 0) {
            throw new BusinessException("该申请已处理，请勿重复审批");
        }
        applyMapper.updateStatus(id, 2, approveUid, LocalDateTime.now());
        messageService.send("student", apply.getSid().longValue(), "approval",
                "退选申请被拒绝", "你申请退选的《" + courseName(apply.getCourseId()) + "》未通过审批", apply.getId().longValue());
    }
}