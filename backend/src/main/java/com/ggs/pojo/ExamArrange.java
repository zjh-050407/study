package com.ggs.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamArrange {
    private Integer id;
    private Integer courseId;
    private Integer semesterId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate examDate;
    private Integer startSection;
    private Integer endSection;
    private String room;
    private Integer invigilatorUid;
    private String type;
    // 关联展示字段
    private String courseName;
    private String semesterName;
    private String invigilatorName;
    private String courseCredit;
}