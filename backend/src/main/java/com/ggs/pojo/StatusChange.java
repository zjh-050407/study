package com.ggs.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusChange {
    private Integer id;
    private Integer sid;
    /** 异动类型：休学/复学/转专业/退学 */
    private String type;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;
    private String reason;
    /** 转专业目标班级 */
    private Integer targetCid;
    /** 经办人 admin.uid */
    private Integer operatorUid;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    // 关联展示字段
    private String studentName;
    private String operatorName;
    private String targetClazzName;
}