package com.ggs.service;

import com.ggs.mapper.ClazzMapper;
import com.ggs.mapper.StudentMapper;
import com.ggs.pojo.Clazz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClazzServiceImpl implements ClazzService {

    @Autowired
    private ClazzMapper clazzMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Override
    public List<Clazz> listClazz() {
        return clazzMapper.listClazz();
    }

    @Override
    public Clazz getById(Integer cid) {
        return clazzMapper.getById(cid);
    }

    @Override
    public int add(Clazz clazz) {
        return clazzMapper.add(clazz);
    }

    @Override
    public int update(Clazz clazz) {
        return clazzMapper.update(clazz);
    }

    @Override
    public int delete(Integer cid) {
        if (studentMapper.countByClazz(cid) > 0) {
            throw new RuntimeException("该班级下存在学生，无法删除");
        }
        return clazzMapper.delete(cid);
    }

    @Override
    public List<Clazz> listByDept(Integer did) {
        return clazzMapper.listByDept(did);
    }
}
