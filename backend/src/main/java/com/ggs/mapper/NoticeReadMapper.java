package com.ggs.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NoticeReadMapper {

    Long getLastId(@Param("userKey") String userKey);

    /** 插入或更新已读游标（仅前进不后退） */
    int upsert(@Param("userKey") String userKey, @Param("lastNoticeId") Long lastNoticeId);
}
