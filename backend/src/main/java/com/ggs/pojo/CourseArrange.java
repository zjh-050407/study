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
    private Integer tid;
    private String classroom;
    private Integer weekDay;
    private Integer section;
    // 关联字段
    private String courseName;
    private String teacherName;
}
