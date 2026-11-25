package com.ggs.mapper;

import com.ggs.pojo.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {

    int insert(Message message);

    /** 批量发给所有在校学生（INSERT ... SELECT，避免全量加载） */
    int insertToAllStudents(@Param("type") String type,
                            @Param("title") String title,
                            @Param("content") String content);

    int unreadCount(@Param("receiverType") String receiverType, @Param("receiverId") Long receiverId);

    List<Message> list(@Param("receiverType") String receiverType,
                       @Param("receiverId") Long receiverId,
                       @Param("offset") int offset,
                       @Param("size") int size);

    int count(@Param("receiverType") String receiverType, @Param("receiverId") Long receiverId);

    int markRead(@Param("id") Long id, @Param("receiverType") String receiverType, @Param("receiverId") Long receiverId);

    int markAllRead(@Param("receiverType") String receiverType, @Param("receiverId") Long receiverId);
}
