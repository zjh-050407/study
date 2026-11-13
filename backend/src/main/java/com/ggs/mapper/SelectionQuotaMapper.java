package com.ggs.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SelectionQuotaMapper {

    /** 懒初始化名额行（已存在则忽略） */
    int ensure(@Param("courseId") Integer courseId, @Param("semesterId") Integer semesterId, @Param("capacity") Integer capacity);

    /** 原子占用名额：used < capacity 才成功 */
    int occupy(@Param("courseId") Integer courseId, @Param("semesterId") Integer semesterId);

    /** 释放名额 */
    int release(@Param("courseId") Integer courseId, @Param("semesterId") Integer semesterId);
}
