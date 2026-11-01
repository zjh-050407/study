package com.ggs.mapper;

import com.ggs.pojo.EvaluationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface EvaluationRecordMapper {

    int insert(EvaluationRecord record);

    /** 防重预检：同一学生对同一课程同一教师是否已评 */
    int countByToken(@Param("taskId") Integer taskId,
                     @Param("courseId") Integer courseId,
                     @Param("teacherUid") Integer teacherUid,
                     @Param("stuToken") String stuToken);

    /** 学生本学期已选且含任课教师的课程列表（按课程×教师拆行） */
    List<Map<String, Object>> listEligible(@Param("sid") Integer sid,
                                           @Param("semesterId") Integer semesterId);

    /** 学生本学期是否选修该课程 */
    int countSelectionInSemester(@Param("sid") Integer sid,
                                 @Param("courseId") Integer courseId,
                                 @Param("semesterId") Integer semesterId);

    /** 教师是否为该课程任课教师（依据排课） */
    int countTeacherOfCourse(@Param("courseId") Integer courseId, @Param("teacherUid") Integer teacherUid);

    /** 统计：按课程×教师汇总平均分与参评人数 */
    List<Map<String, Object>> listStats(@Param("taskId") Integer taskId);

    /** 某课程×教师的匿名建议列表（仅文字，无学生身份） */
    List<String> listSuggestions(@Param("taskId") Integer taskId,
                                 @Param("courseId") Integer courseId,
                                 @Param("teacherUid") Integer teacherUid);
}