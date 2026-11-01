package com.ggs.service;

import com.ggs.exception.BusinessException;
import com.ggs.mapper.CourseSelectionMapper;
import com.ggs.mapper.SelectionApplyMapper;
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
        // 删除选课记录（若已被教师代选侧删除则忽略）
        selectionMapper.deleteBySidAndCourse(apply.getSid(), apply.getCourseId());
        applyMapper.updateStatus(id, 1, approveUid, LocalDateTime.now());
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
    }
}