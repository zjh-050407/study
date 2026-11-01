package com.ggs.mapper;

import com.ggs.pojo.GraduateAudit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GraduateAuditMapper {

    int insert(GraduateAudit audit);

    /** 按学生查审核历史（sid 为空时查全部） */
    List<GraduateAudit> listHistory(@Param("sid") Integer sid);
}