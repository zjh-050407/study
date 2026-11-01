package com.ggs.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelectionApply {
    private Integer id;
    private Integer sid;
    private Integer courseId;
    private String reason;
    /** 0=待审批 1=已通过 2=已拒绝 */
    private Integer status;
    private LocalDateTime applyTime;
    private Integer approveUid;
    private LocalDateTime approveTime;
    // 关联字段
    private String studentName;
    private String courseName;
    private String clazzName;
    private Double credit;
}