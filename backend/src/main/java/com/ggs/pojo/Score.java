package com.ggs.pojo;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Score {
    private Integer scoreId;

    @NotNull(message = "学生不能为空")
    private Integer sid;

    @NotNull(message = "课程不能为空")
    private Integer courseId;

    @DecimalMin(value = "0", message = "平时成绩不能小于0")
    @DecimalMax(value = "100", message = "平时成绩不能大于100")
    private Double usual;

    @DecimalMin(value = "0", message = "期中成绩不能小于0")
    @DecimalMax(value = "100", message = "期中成绩不能大于100")
    private Double mid;

    @DecimalMin(value = "0", message = "期末成绩不能小于0")
    @DecimalMax(value = "100", message = "期末成绩不能大于100")
    private Double finalScore;

    private Double total;
    private Double gp;
    // 关联字段
    private String studentName;
    private String courseName;
    private String clazzName;
    private Integer semesterId;
    private String semesterName;
    private Double credit;
}
