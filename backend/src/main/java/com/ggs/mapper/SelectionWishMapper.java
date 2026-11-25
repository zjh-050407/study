package com.ggs.mapper;

import com.ggs.pojo.SelectionWish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SelectionWishMapper {

    /** 覆盖式提交：先删旧志愿再插入 */
    int deleteByRoundAndSid(@Param("roundId") Integer roundId, @Param("sid") Integer sid);

    int insert(SelectionWish wish);

    /** 学生本人志愿（含课程名/容量/已选人数） */
    List<SelectionWish> listByRoundAndSid(@Param("roundId") Integer roundId, @Param("sid") Integer sid);

    /** 轮次全部志愿（投档用，按优先级+提交时间排序） */
    List<SelectionWish> listByRound(@Param("roundId") Integer roundId);
}
