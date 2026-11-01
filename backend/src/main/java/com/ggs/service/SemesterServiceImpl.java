package com.ggs.service;

import com.ggs.exception.BusinessException;
import com.ggs.mapper.SemesterMapper;
import com.ggs.pojo.Semester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SemesterServiceImpl implements SemesterService {

    @Autowired
    private SemesterMapper semesterMapper;

    @Override
    public List<Semester> listAll() {
        return semesterMapper.listAll();
    }

    @Override
    public Semester getCurrent() {
        return semesterMapper.getCurrent();
    }

    @Override
    public void add(Semester semester) {
        if (semester.getName() == null || semester.getName().trim().isEmpty()) {
            throw new BusinessException("学期名称不能为空");
        }
        if (semesterMapper.countByName(semester.getName().trim(), null) > 0) {
            throw new BusinessException("学期名称已存在");
        }
        semester.setName(semester.getName().trim());
        semesterMapper.insert(semester);
    }

    @Override
    public void update(Semester semester) {
        if (semester.getId() == null) {
            throw new BusinessException("参数错误：缺少学期ID");
        }
        if (semester.getName() == null || semester.getName().trim().isEmpty()) {
            throw new BusinessException("学期名称不能为空");
        }
        if (semesterMapper.countByName(semester.getName().trim(), semester.getId()) > 0) {
            throw new BusinessException("学期名称已存在");
        }
        semester.setName(semester.getName().trim());
        semesterMapper.update(semester);
    }

    @Override
    public void delete(int id) {
        Semester semester = semesterMapper.getById(id);
        if (semester == null) {
            throw new BusinessException("学期不存在");
        }
        if (semester.getIsCurrent() != null && semester.getIsCurrent() == 1) {
            throw new BusinessException("当前学期不能删除");
        }
        semesterMapper.delete(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setCurrent(int id) {
        Semester semester = semesterMapper.getById(id);
        if (semester == null) {
            throw new BusinessException("学期不存在");
        }
        semesterMapper.clearCurrent();
        semesterMapper.setCurrent(id);
    }
}