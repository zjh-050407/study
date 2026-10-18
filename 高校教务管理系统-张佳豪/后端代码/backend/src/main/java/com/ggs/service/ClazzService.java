package com.ggs.service;

import com.ggs.pojo.Clazz;

import java.util.List;

public interface ClazzService {
    List<Clazz> listClazz();
    Clazz getById(Integer cid);
    int add(Clazz clazz);
    int update(Clazz clazz);
    int delete(Integer cid);
    List<Clazz> listByDept(Integer did);
}
