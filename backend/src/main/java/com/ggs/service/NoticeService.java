package com.ggs.service;

import com.ggs.pojo.Notice;
import com.ggs.vo.ResultVO;

import java.util.List;

public interface NoticeService {
    ResultVO.PageResult<Notice> listNotice(int page, int size);

    List<Notice> latest(int limit);

    void add(Notice notice);

    void delete(Integer id);
}