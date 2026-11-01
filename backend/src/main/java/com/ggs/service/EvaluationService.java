package com.ggs.service;

import com.ggs.pojo.EvaluationRecord;
import com.ggs.pojo.EvaluationTask;

import java.util.List;
import java.util.Map;

public interface EvaluationService {

    /** 发起评教任务（semesterId 为空则取当前学期） */
    void createTask(EvaluationTask task);

    /** 关闭评教任务 */
    void closeTask(Integer id);

    List<EvaluationTask> listTasks();

    /** 学生端：当前学期评教窗口信息（活动任务/是否开启/学期名） */
    Map<String, Object> activeWindow();

    /** 学生端：本学期可评课程列表（含已评标记） */
    List<Map<String, Object>> listEligibleForStudent(Integer sid);

    /** 学生提交匿名评教 */
    void submit(Integer sid, EvaluationRecord record);

    /** 教师/管理员：评教统计（含匿名建议） */
    List<Map<String, Object>> stats(Integer taskId);

    /** 管理员：导出 CSV */
    String exportCsv(Integer taskId);
}