package com.ggs.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private Long id;

    /** 接收人类型：admin/teacher/student */
    private String receiverType;

    /** 接收人ID（admin/teacher 为 uid，student 为 sid） */
    private Long receiverId;

    /** 类型：system/approval/notice/selection */
    private String type;

    private String title;

    private String content;

    /** 关联业务ID（如申请单ID） */
    private Long bizId;

    private Integer isRead;

    private LocalDateTime createTime;
}
