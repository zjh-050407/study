package com.ggs.service;

import com.ggs.pojo.Score;
import com.ggs.pojo.ScoreAppeal;

import java.util.List;
import java.util.Map;

public interface ScoreService {
    List<Score> listScore();
    List<Score> listByStudent(Integer sid);
    List<Score> listByCourse(Integer courseId);
    int add(Score score);
    int update(Score score);
    int delete(Integer scoreId);
    List<Map<String, Object>> avgByCourse();
    List<Map<String, Object>> countByGrade();

    /**
     * 学生本人成绩：按学期过滤（null=全部，0=未分学期），返回 list/gpa/totalCredit
     */
    Map<String, Object> myScores(Integer sid, Integer semesterId);

    /* ==================== 批次N：成绩流程 ==================== */

    /** 批量提交：草稿 -> 待审核 */
    int submit(List<Integer> ids);

    /** 批量发布：待审核 -> 已发布，并通知学生 */
    int publish(List<Integer> ids);

    /** 学生申诉：已发布 -> 复核中 */
    void appeal(Integer scoreId, Integer sid, String reason);

    /** 管理端待处理申诉列表 */
    List<ScoreAppeal> listAppealPending();

    /** 学生本人申诉记录 */
    List<ScoreAppeal> listAppealByStudent(Integer sid);

    /** 复核处理：approve（可选改分）/reject，恢复已发布并通知学生 */
    void handleAppeal(Integer appealId, String action, Double newTotal, String handleNote, Long handleUid);
}
