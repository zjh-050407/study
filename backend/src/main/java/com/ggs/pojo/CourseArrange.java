package com.ggs.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseArrange {
    private Integer arrangeId;
    private Integer courseId;
    private Integer semesterId;
    private Integer tid;
    private String classroom;
    private Integer weekDay;
    private Integer section;
    // 关联字段
    private String courseName;
    private String teacherName;
    private String semesterName;
    // 关联教学任务（批次J：排课成功后推进任务状态，不落库）
    private Integer taskId;
}
