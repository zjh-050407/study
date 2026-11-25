package com.ggs.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeachingWeek {

    private Integer id;

    /** 学期ID（0=未分学期哨兵） */
    private Integer semesterId;

    /** 第几教学周 */
    private Integer weekNo;

    private LocalDate startDate;

    private LocalDate endDate;

    /** 备注（如 考试周/放假） */
    private String note;
}
