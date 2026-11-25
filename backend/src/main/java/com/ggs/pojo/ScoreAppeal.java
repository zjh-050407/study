package com.ggs.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreAppeal {

    private Integer id;

    private Integer scoreId;

    private Integer sid;

    private String reason;

    /** 0待处理/1通过/2驳回 */
    private Integer status;

    /** 复核通过后的新总评（可选，不传则维持原分） */
    private Double newTotal;

    private String handleNote;

    private LocalDateTime createTime;

    private LocalDateTime handleTime;

    private Long handleUid;

    // 关联展示字段
    private String studentName;
    private String courseName;
    private Double total;
    private Integer courseId;
}
