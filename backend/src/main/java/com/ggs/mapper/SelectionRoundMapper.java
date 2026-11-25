package com.ggs.mapper;

import com.ggs.pojo.SelectionRound;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SelectionRoundMapper {

    int insert(SelectionRound round);

    SelectionRound getById(@Param("id") Integer id);

    List<SelectionRound> list(@Param("semesterId") Integer semesterId);

    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    /** 指定类型进行中的轮次（约束保证至多一个） */
    SelectionRound findRunning(@Param("type") Integer type);

    /** 任意类型进行中的轮次数（同类型唯一约束校验用） */
    int countRunningByType(@Param("type") Integer type, @Param("semesterId") Integer semesterId);
}
