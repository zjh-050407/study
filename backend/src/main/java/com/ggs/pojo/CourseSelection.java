package com.ggs.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseSelection {
    private Integer id;
    private Integer sid;
    private Integer courseId;
    private Integer uid;
    private LocalDateTime createTime;
    // 关联字段
    private String studentName;
    private String clazzName;
    private String courseName;
    private Double credit;
}