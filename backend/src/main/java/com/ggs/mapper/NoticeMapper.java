package com.ggs.mapper;

import com.ggs.pojo.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeMapper {
    int insert(Notice notice);

    int delete(@Param("id") Integer id);

    List<Notice> listByPage(@Param("offset") int offset, @Param("size") int size);

    int countAll();

    List<Notice> listLatest(@Param("limit") int limit);
}