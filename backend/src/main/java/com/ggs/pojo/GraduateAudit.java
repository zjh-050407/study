package com.ggs.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GraduateAudit {
    private Integer id;
    private Integer sid;
    private Integer semesterId;
    /** 结论：通过/不通过 */
    private String conclusion;
    /** 审核明细 JSON 字符串 */
    private String detailJson;
    private Integer auditorUid;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
    // 关联展示字段
    private String studentName;
    private String semesterName;
    private String auditorName;
}