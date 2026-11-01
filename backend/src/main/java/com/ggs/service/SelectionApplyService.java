package com.ggs.service;

import com.ggs.pojo.SelectionApply;

import java.util.List;

public interface SelectionApplyService {

    /** 学生提交退选申请（sid 从 token 绑定） */
    void apply(Integer sid, Integer courseId, String reason);

    /** 学生本人申请列表 */
    List<SelectionApply> listByStudent(Integer sid);

    /** 教师端待审批列表 */
    List<SelectionApply> listPending();

    /** 审批通过：删除选课记录并更新申请单状态 */
    void approve(Integer id, Integer approveUid);

    /** 拒绝 */
    void reject(Integer id, Integer approveUid);
}