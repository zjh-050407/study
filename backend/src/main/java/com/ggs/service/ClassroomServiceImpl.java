package com.ggs.service;

import com.ggs.exception.BusinessException;
import com.ggs.mapper.ClassroomMapper;
import com.ggs.pojo.Classroom;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Service
public class ClassroomServiceImpl implements ClassroomService {

    private static final List<String> TYPES = Arrays.asList("普通教室", "实验室", "机房", "报告厅");

    @Autowired
    private ClassroomMapper classroomMapper;

    @Override
    public ResultVO.PageResult<Classroom> list(int page, int size) {
        int offset = (page - 1) * size;
        List<Classroom> list = classroomMapper.listByPage(offset, size);
        int total = classroomMapper.countAll();
        return new ResultVO.PageResult<>(list, total, page, size);
    }

    @Override
    public Classroom getById(Integer id) {
        return classroomMapper.getById(id);
    }

    @Override
    public void add(Classroom classroom) {
        validate(classroom);
        if (classroomMapper.countByName(classroom.getName(), null) > 0) {
            throw new BusinessException("教室名称已存在，请勿重复添加");
        }
        if (classroom.getStatus() == null) {
            classroom.setStatus(1);
        }
        classroomMapper.insert(classroom);
    }

    @Override
    public void update(Classroom classroom) {
        if (classroom.getId() == null || classroomMapper.getById(classroom.getId()) == null) {
            throw new BusinessException("教室不存在");
        }
        if (classroom.getStatus() == null) {
            classroom.setStatus(1);
        }
        validate(classroom);
        if (classroomMapper.countByName(classroom.getName(), classroom.getId()) > 0) {
            throw new BusinessException("教室名称已存在，请勿重复添加");
        }
        classroomMapper.update(classroom);
    }

    @Override
    public void delete(Integer id) {
        if (id == null || classroomMapper.delete(id) == 0) {
            throw new BusinessException("删除失败，教室不存在");
        }
    }

    private void validate(Classroom classroom) {
        if (classroom == null || !StringUtils.hasText(classroom.getName())) {
            throw new BusinessException("请填写教室名称");
        }
        if (classroom.getName().length() > 100) {
            throw new BusinessException("教室名称不能超过100字");
        }
        if (classroom.getCapacity() == null || classroom.getCapacity() < 1 || classroom.getCapacity() > 500) {
            throw new BusinessException("教室容量需在1-500之间");
        }
        if (StringUtils.hasText(classroom.getType()) && !TYPES.contains(classroom.getType())) {
            throw new BusinessException("教室类型仅支持：普通教室/实验室/机房/报告厅");
        }
    }
}
