package com.ggs.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentImportDTO {
    @ExcelProperty("姓名")
    @NotBlank(message = "姓名不能为空")
    private String name;

    @ExcelProperty("年龄")
    @NotNull(message = "年龄不能为空")
    @Min(value = 1, message = "年龄不合法")
    @Max(value = 120, message = "年龄不合法")
    private Integer age;

    @ExcelProperty("性别")
    @NotBlank(message = "性别不能为空")
    private String sex;

    @ExcelProperty("班级")
    @NotBlank(message = "班级不能为空")
    private String clazzName;
}
