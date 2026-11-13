package com.ggs.mapper;

import com.ggs.pojo.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OperationLogMapper {
    int insert(OperationLog log);
    List<OperationLog> list();

    List<OperationLog> listByPage(@Param("offset") int offset,
                                  @Param("size") int size,
                                  @Param("username") String username,
                                  @Param("operation") String operation,
                                  @Param("startTime") String startTime,
                                  @Param("endTime") String endTime);

    int countByFilter(@Param("username") String username,
                      @Param("operation") String operation,
                      @Param("startTime") String startTime,
                      @Param("endTime") String endTime);
}
