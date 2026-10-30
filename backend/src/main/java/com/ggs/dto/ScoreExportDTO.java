package com.ggs.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ScoreExportDTO {
    @ExcelProperty("学号")
    private Integer sid;

    @ExcelProperty("姓名")
    private String studentName;

    @ExcelProperty("班级")
    private String clazzName;

    @ExcelProperty("课程")
    private String courseName;

    @ExcelProperty("平时成绩")
    private Double usual;

    @ExcelProperty("期中成绩")
    private Double mid;

    @ExcelProperty("期末成绩")
    private Double finalScore;

    @ExcelProperty("总评")
    private Double total;

    @ExcelProperty("绩点")
    private Double gp;
}
