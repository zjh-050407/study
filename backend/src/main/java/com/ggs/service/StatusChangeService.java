package com.ggs.service;

import com.ggs.pojo.StatusChange;

import java.util.List;
import java.util.Map;

public interface StatusChangeService {

    /** 登记学籍异动（休学/复学/转专业/退学）；转专业会联动更新学生班级 */
    void register(StatusChange change, Integer operatorUid);

    /** 全部异动记录 */
    List<StatusChange> listAll();

    /** 指定学生的异动历史 */
    List<StatusChange> listBySid(Integer sid);

    /**
     * 学生当前学籍状态：返回阻断状态名（休学/退学），无阻断返回 null。
     * 状态由最近一次异动（按生效日期取最新）推导：休学/退学阻断，复学/转专业不阻断。
     */
    String blockedStatus(Integer sid);

    /** 学生当前学籍状态详情（无记录时 type=在校） */
    Map<String, Object> currentStatus(Integer sid);
}