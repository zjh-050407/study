package com.ggs.service;

import com.ggs.pojo.Message;

import java.util.List;
import java.util.Map;

public interface MessageService {

    /** 给单个接收人发消息（receiverType: admin/student，receiverId: uid 或 sid） */
    void send(String receiverType, Long receiverId, String type, String title, String content, Long bizId);

    /** 群发给某角色的全部账号（admin 表 role 字段：admin/professor/lecturer） */
    void sendToRole(String role, String type, String title, String content, Long bizId);

    /** 群发给所有在校学生 */
    void sendToAllStudents(String type, String title, String content);

    /** 未读个人消息数 */
    int unreadCount(String receiverType, Long receiverId);

    /** 未读公告数（基于已读游标） */
    int noticeUnread(String userKey);

    List<Message> list(String receiverType, Long receiverId, int page, int size);

    int count(String receiverType, Long receiverId);

    void markRead(Long id, String receiverType, Long receiverId);

    void markAllRead(String receiverType, Long receiverId);

    /** 标记公告已读（游标推进到最新公告ID） */
    void markNoticeRead(String userKey);

    /** 待办聚合：按角色返回待办数量 */
    Map<String, Object> todoSummary(String role);
}
