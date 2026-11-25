package com.ggs.mapper;

import com.ggs.pojo.Score;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ScoreMapper {
    List<Score> listScore();
    List<Score> listByPage(@Param("offset") int offset, @Param("size") int size, @Param("did") Integer did, @Param("status") Integer status);
    int countAll(@Param("did") Integer did, @Param("status") Integer status);
    List<Score> listByStudent(@Param("sid") Integer sid);
    List<Score> listByCourse(@Param("courseId") Integer courseId);
    Score getById(@Param("scoreId") Integer scoreId);
    int add(Score score);
    int update(Score score);
    int delete(@Param("scoreId") Integer scoreId);
    /** 批量状态流转（条件更新防越级）：fromStatus -> toStatus */
    int updateStatusBatch(@Param("ids") List<Integer> ids,
                          @Param("fromStatus") Integer fromStatus,
                          @Param("toStatus") Integer toStatus);
    /** 单条状态流转：fromStatus -> toStatus（复核通过后恢复已发布） */
    int updateStatus(@Param("scoreId") Integer scoreId,
                     @Param("fromStatus") Integer fromStatus,
                     @Param("toStatus") Integer toStatus);
    /** 复核改分 */
    int updateTotal(@Param("scoreId") Integer scoreId,
                    @Param("total") Double total,
                    @Param("gp") Double gp);
    int countByStudentAndCourse(@Param("sid") Integer sid,
                                @Param("courseId") Integer courseId,
                                @Param("semesterId") Integer semesterId,
                                @Param("excludeId") Integer excludeId);
    List<Map<String, Object>> avgByCourse();
    List<Map<String, Object>> countByGrade();

    /**
     * 学生按学期查询成绩；semesterId=0 表示未分学期（semester_id IS NULL），null 表示全部学期
     * 学生视图仅返回已发布/复核中的成绩
     */
    List<Score> listByStudentAndSemester(@Param("sid") Integer sid, @Param("semesterId") Integer semesterId);
}
