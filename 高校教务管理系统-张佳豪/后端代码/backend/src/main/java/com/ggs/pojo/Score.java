package com.ggs.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Score {
    private Integer scoreId;
    private Integer sid;
    private Integer courseId;
    private Double usual;
    private Double mid;
    private Double finalScore;
    private Double total;
    private Double gp;
    // 关联字段
    private String studentName;
    private String courseName;
    private String clazzName;
}
