package com.ggs.service;

import com.ggs.pojo.Classroom;
import com.ggs.vo.ResultVO;

public interface ClassroomService {
    ResultVO.PageResult<Classroom> list(int page, int size);
    Classroom getById(Integer id);
    void add(Classroom classroom);
    void update(Classroom classroom);
    void delete(Integer id);
}
