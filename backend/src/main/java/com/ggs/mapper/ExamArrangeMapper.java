package com.ggs.mapper;

import com.ggs.pojo.ExamArrange;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface ExamArrangeMapper {

    List<ExamArrange> listAll();

    int insert(ExamArrange exam);

    int update(ExamArrange exam);

    int delete(@Param("id") Integer id);

    /** 同课程同学期已安排的考试数（排除自身），用于「同课程不重复排」校验 */
    int countByCourseSemester(@Param("excludeId") Integer excludeId,
                              @Param("courseId") Integer courseId,
                              @Param("semesterId") Integer semesterId);

    /** 同考场同日期时段重叠的考试（排除自身） */
    List<Map<String, Object>> listRoomConflicts(@Param("excludeId") Integer excludeId,
                                                @Param("room") String room,
                                                @Param("examDate") LocalDate examDate,
                                                @Param("startSection") Integer startSection,
                                                @Param("endSection") Integer endSection);

    /** 同监考教师同日期时段重叠的考试（排除自身） */
    List<Map<String, Object>> listTeacherConflicts(@Param("excludeId") Integer excludeId,
                                                   @Param("invigilatorUid") Integer invigilatorUid,
                                                   @Param("examDate") LocalDate examDate,
                                                   @Param("startSection") Integer startSection,
                                                   @Param("endSection") Integer endSection);

    /** 学生本人考试安排：按其选课记录关联本学期的考试 */
    List<Map<String, Object>> listMyExams(@Param("sid") Integer sid);
}