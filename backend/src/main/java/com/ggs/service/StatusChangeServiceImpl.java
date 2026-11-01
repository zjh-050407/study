package com.ggs.service;

import com.ggs.exception.BusinessException;
import com.ggs.mapper.ClazzMapper;
import com.ggs.mapper.StatusChangeMapper;
import com.ggs.mapper.StudentMapper;
import com.ggs.pojo.StatusChange;
import com.ggs.pojo.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatusChangeServiceImpl implements StatusChangeService {

    /** 合法的异动类型 */
    private static final List<String> TYPES = List.of("休学", "复学", "转专业", "退学");

    @Autowired
    private StatusChangeMapper statusMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private ClazzMapper clazzMapper;

    @Override
    public void register(StatusChange change, Integer operatorUid) {
        if (change.getSid() == null) {
            throw new BusinessException("请选择学生");
        }
        Student student = studentMapper.getById(change.getSid());
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        String type = change.getType() == null ? "" : change.getType().trim();
        if (!TYPES.contains(type)) {
            throw new BusinessException("异动类型仅支持：休学/复学/转专业/退学");
        }
        change.setType(type);
        if (change.getEffectiveDate() == null) {
            throw new BusinessException("请填写异动生效日期");
        }
        if (change.getReason() != null && change.getReason().trim().length() > 500) {
            throw new BusinessException("原因说明不能超过500字");
        }
        if (change.getReason() != null) {
            change.setReason(change.getReason().trim().isEmpty() ? null : change.getReason().trim());
        }

        // 转专业：联动更新班级字段
        if ("转专业".equals(type)) {
            Integer targetCid = change.getTargetCid();
            if (targetCid == null || clazzMapper.getById(targetCid) == null) {
                throw new BusinessException("转专业必须选择有效的目标班级");
            }
            if (targetCid.equals(student.getCid())) {
                throw new BusinessException("目标班级与学生当前班级相同，无需转专业");
            }
            studentMapper.updateCid(change.getSid(), targetCid);
            change.setTargetCid(targetCid);
        } else {
            change.setTargetCid(null);
        }

        change.setOperatorUid(operatorUid);
        change.setCreateTime(LocalDateTime.now());
        statusMapper.insert(change);
    }

    @Override
    public List<StatusChange> listAll() {
        return statusMapper.listAll();
    }

    @Override
    public List<StatusChange> listBySid(Integer sid) {
        return statusMapper.listBySid(sid);
    }

    @Override
    public String blockedStatus(Integer sid) {
        StatusChange latest = statusMapper.getLatestBySid(sid);
        if (latest == null) {
            return null;
        }
        if ("休学".equals(latest.getType()) || "退学".equals(latest.getType())) {
            return latest.getType();
        }
        return null;
    }

    @Override
    public Map<String, Object> currentStatus(Integer sid) {
        StatusChange latest = statusMapper.getLatestBySid(sid);
        Map<String, Object> result = new HashMap<>();
        if (latest == null) {
            result.put("type", "在校");
            result.put("effectiveDate", null);
            return result;
        }
        String type = latest.getType();
        // 复学/转专业之后视为正常在校
        result.put("type", ("复学".equals(type) || "转专业".equals(type)) ? "在校" : type);
        result.put("effectiveDate", latest.getEffectiveDate() == null ? null : latest.getEffectiveDate().toString());
        result.put("latest", latest);
        return result;
    }
}