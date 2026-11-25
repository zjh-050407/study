package com.ggs.mapper;

import com.ggs.pojo.ScoreAppeal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ScoreAppealMapper {

    int insert(ScoreAppeal appeal);

    ScoreAppeal getById(@Param("id") Integer id);

    /** 学生本人申诉记录 */
    List<ScoreAppeal> listByStudent(@Param("sid") Integer sid);

    /** 管理端待处理申诉（含学生/课程/当前成绩） */
    List<ScoreAppeal> listPending();

    /** 同一成绩是否已有待处理申诉 */
    int countPendingByScore(@Param("scoreId") Integer scoreId);

    int updateStatus(@Param("id") Integer id,
                     @Param("status") Integer status,
                     @Param("newTotal") Double newTotal,
                     @Param("handleNote") String handleNote,
                     @Param("handleUid") Long handleUid,
                     @Param("handleTime") LocalDateTime handleTime);
}
