package com.ggs.service;

import com.ggs.pojo.SelectionRound;
import com.ggs.pojo.SelectionWish;

import java.util.List;
import java.util.Map;

public interface SelectionRoundService {

    /** 轮次列表（按学期） */
    List<SelectionRound> list(Integer semesterId);

    /** 创建轮次（教授/管理员） */
    void create(SelectionRound round);

    /** 轮次状态流转：0未开始 -> 1进行中 -> 2已结束（同类型进行中唯一） */
    void updateStatus(Integer id, Integer status);

    /** 当前进行中轮次：{first: 初选, second: 补退选}（各至多一个） */
    Map<String, Object> current();

    /** 学生提交志愿（覆盖式，按 courseIds 顺序定优先级） */
    void submitWishes(Integer sid, Integer roundId, List<Integer> courseIds);

    /** 学生本人志愿列表 */
    List<SelectionWish> listWishes(Integer sid, Integer roundId);

    /** 初选投档：逐志愿录取，返回统计 {total, admitted, failed} */
    Map<String, Object> allocate(Integer roundId);
}
