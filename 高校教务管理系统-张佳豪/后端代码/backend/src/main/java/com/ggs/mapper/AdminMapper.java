package com.ggs.mapper;

import com.ggs.pojo.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminMapper {

    Admin login(@Param("username") String username, @Param("password") String password);

    int updateAvatar(@Param("uid") Long uid, @Param("avatar") String avatar);

    Admin getById(@Param("uid") Long uid);

    int updatePassword(@Param("uid") Long uid, @Param("password") String password);

    Admin findByUsername(@Param("username") String username);

    int insert(Admin admin);

    Long selectMaxUid();

    int insertNextUid(Admin admin);
}