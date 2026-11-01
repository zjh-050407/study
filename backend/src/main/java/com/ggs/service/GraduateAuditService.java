package com.ggs.service;

import java.util.List;
import java.util.Map;

public interface GraduateAuditService {

    /**
     * 对指定学生执行毕业审核并留档。
     *
     * @param sid                 学生ID
     * @param requiredTotalCredit 总学分要求（学生无培养方案时生效；有培养方案时按方案要求）
     * @param requiredGpa         绩点要求（默认 2.0）
     * @param auditorUid          审核人 admin.uid
     * @return 审核结果（结论 + 缺项明细 + 留档记录ID）
     */
    Map<String, Object> audit(Integer sid, Double requiredTotalCredit, Double requiredGpa, Integer auditorUid);

    /** 审核历史（sid 为空时返回全部） */
    List<Map<String, Object>> history(Integer sid);
}