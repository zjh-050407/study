package com.ggs.mapper;

import com.ggs.pojo.RegisterRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RegisterRequestMapper {

    RegisterRequest findByUsername(@Param("username") String username);

    int insert(RegisterRequest request);

    List<RegisterRequest> selectByStatus(@Param("status") String status);

    RegisterRequest selectById(@Param("id") Integer id);

    int updateStatus(@Param("id") Integer id, @Param("status") String status);
}