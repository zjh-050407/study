package com.ggs.service;

import com.ggs.mapper.AdminMapper;
import com.ggs.mapper.MessageMapper;
import com.ggs.mapper.NoticeMapper;
import com.ggs.mapper.NoticeReadMapper;
import com.ggs.mapper.RegisterRequestMapper;
import com.ggs.mapper.SelectionApplyMapper;
import com.ggs.pojo.Admin;
import com.ggs.pojo.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private NoticeReadMapper noticeReadMapper;

    @Autowired
    private RegisterRequestMapper registerRequestMapper;

    @Autowired
    private SelectionApplyMapper selectionApplyMapper;

    @Override
    public void send(String receiverType, Long receiverId, String type, String title, String content, Long bizId) {
        if (receiverId == null) {
            return;
        }
        Message message = new Message();
        message.setReceiverType(receiverType);
        message.setReceiverId(receiverId);
        message.setType(type == null ? "system" : type);
        message.setTitle(title);
        message.setContent(content);
        message.setBizId(bizId);
        message.setIsRead(0);
        messageMapper.insert(message);
    }

    @Override
    public void sendToRole(String role, String type, String title, String content, Long bizId) {
        List<Admin> accounts = adminMapper.listByRole(role);
        for (Admin account : accounts) {
            send("admin", account.getUid(), type, title, content, bizId);
        }
    }

    @Override
    public void sendToAllStudents(String type, String title, String content) {
        messageMapper.insertToAllStudents(type, title, content);
    }

    @Override
    public int unreadCount(String receiverType, Long receiverId) {
        return messageMapper.unreadCount(receiverType, receiverId);
    }

    @Override
    public int noticeUnread(String userKey) {
        Long maxId = noticeMapper.selectMaxPublishedId();
        if (maxId == null || maxId == 0) {
            return 0;
        }
        Long lastId = noticeReadMapper.getLastId(userKey);
        if (lastId == null) {
            // 从未读过公告：全部未读
            return noticeMapper.countPublishedAfter(0L);
        }
        if (lastId >= maxId) {
            return 0;
        }
        return noticeMapper.countPublishedAfter(lastId);
    }

    @Override
    public List<Message> list(String receiverType, Long receiverId, int page, int size) {
        int offset = (page - 1) * size;
        return messageMapper.list(receiverType, receiverId, offset, size);
    }

    @Override
    public int count(String receiverType, Long receiverId) {
        return messageMapper.count(receiverType, receiverId);
    }

    @Override
    public void markRead(Long id, String receiverType, Long receiverId) {
        messageMapper.markRead(id, receiverType, receiverId);
    }

    @Override
    public void markAllRead(String receiverType, Long receiverId) {
        messageMapper.markAllRead(receiverType, receiverId);
    }

    @Override
    public void markNoticeRead(String userKey) {
        Long maxId = noticeMapper.selectMaxPublishedId();
        if (maxId != null && maxId > 0) {
            noticeReadMapper.upsert(userKey, maxId);
        }
    }

    @Override
    public Map<String, Object> todoSummary(String role) {
        Map<String, Object> map = new HashMap<>();
        map.put("role", role);
        if ("admin".equals(role)) {
            map.put("registerPending", registerRequestMapper.countPending());
            map.put("applyPending", selectionApplyMapper.countPending());
        } else if ("professor".equals(role)) {
            map.put("applyPending", selectionApplyMapper.countPending());
        } else {
            map.put("applyPending", 0);
            map.put("registerPending", 0);
        }
        return map;
    }
}
