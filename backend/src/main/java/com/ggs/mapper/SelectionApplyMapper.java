package com.ggs.mapper;

import com.ggs.pojo.SelectionApply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SelectionApplyMapper {

    int insert(SelectionApply apply);

    SelectionApply getById(@Param("id") Integer id);

    /** 学生本人退选申请列表 */
    List<SelectionApply> listByStudent(@Param("sid") Integer sid);

    /** 待审批列表（含学生/课程/班级关联信息） */
    List<SelectionApply> listPending();

    int countPendingBySidAndCourse(@Param("sid") Integer sid, @Param("courseId") Integer courseId);

    int updateStatus(@Param("id") Integer id,
                     @Param("status") Integer status,
                     @Param("approveUid") Integer approveUid,
                     @Param("approveTime") LocalDateTime approveTime);

    int countSelection(@Param("sid") Integer sid, @Param("courseId") Integer courseId);
}