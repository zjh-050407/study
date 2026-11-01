package com.ggs.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EvaluationRecord {
    private Integer id;
    private Integer taskId;
    private Integer courseId;
    private Integer teacherUid;
    private String stuToken; // 学生匿名令牌，不存学号
    private Integer dim1; // 教学态度 1-5
    private Integer dim2; // 教学内容 1-5
    private Integer dim3; // 教学方法 1-5
    private Integer dim4; // 教学效果 1-5
    private String suggestion;
    private LocalDateTime createTime;
}