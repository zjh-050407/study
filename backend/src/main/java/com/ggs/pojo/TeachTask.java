package com.ggs.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeachTask {
    private Integer id;
    private Integer courseId;
    private Integer semesterId;
    private Integer cid;
    private Integer tid;
    private Integer status; // 0=未排课 1=已排课
    private LocalDateTime createTime;
    // 关联字段
    private String courseName;
    private String semesterName;
    private String clazzName;
    private String teacherName;
    private Double credit;
}
