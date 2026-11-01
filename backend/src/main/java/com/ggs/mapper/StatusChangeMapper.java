package com.ggs.mapper;

import com.ggs.pojo.StatusChange;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StatusChangeMapper {

    int insert(StatusChange change);

    List<StatusChange> listAll();

    List<StatusChange> listBySid(@Param("sid") Integer sid);

    /** 该生最近一次异动（按生效日期、登记顺序取最新） */
    StatusChange getLatestBySid(@Param("sid") Integer sid);
}