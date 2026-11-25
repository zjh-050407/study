package com.ggs.mapper;

import com.ggs.pojo.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeMapper {
    int insert(Notice notice);

    int delete(@Param("id") Integer id);

    List<Notice> listByPage(@Param("offset") int offset, @Param("size") int size);

    int countAll();

    List<Notice> listLatest(@Param("limit") int limit);

    /** 最新公告ID（已发布），未读公告角标用 */
    Long selectMaxPublishedId();

    /** 统计 ID 大于 afterId 的已发布公告数 */
    int countPublishedAfter(@Param("afterId") Long afterId);
}