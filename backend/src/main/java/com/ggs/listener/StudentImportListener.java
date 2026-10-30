package com.ggs.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.ggs.dto.StudentImportDTO;
import com.ggs.pojo.Clazz;
import com.ggs.pojo.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 两阶段导入：
 * 阶段1 全部行读入内存并逐行校验，收集错误清单；
 * 阶段2 由 service 在事务中批量落库（见 StudentServiceImpl.batchAddStudents）。
 * 任一错误存在时调用方不落库，避免半途失败产生脏数据。
 */
public class StudentImportListener implements ReadListener<StudentImportDTO> {

    private final Map<String, Integer> clazzNameToId;
    private final List<StudentImportDTO> cachedDataList = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    public StudentImportListener(List<Clazz> clazzList) {
        this.clazzNameToId = clazzList.stream()
                .collect(Collectors.toMap(Clazz::getName, Clazz::getCid, (a, b) -> a));
    }

    @Override
    public void invoke(StudentImportDTO data, AnalysisContext context) {
        cachedDataList.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 校验与落库由调用方显式执行（两阶段）
    }

    public void validateAll() {
        for (int i = 0; i < cachedDataList.size(); i++) {
            String error = validate(cachedDataList.get(i));
            if (error != null) {
                errors.add("第" + (i + 1) + "行：" + error);
            }
        }
    }

    public List<Student> toStudents() {
        List<Student> students = new ArrayList<>(cachedDataList.size());
        for (StudentImportDTO dto : cachedDataList) {
            Student student = new Student();
            student.setName(dto.getName());
            student.setAge(dto.getAge());
            student.setSex(dto.getSex());
            student.setCid(clazzNameToId.get(dto.getClazzName()));
            students.add(student);
        }
        return students;
    }

    private String validate(StudentImportDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return "姓名不能为空";
        }
        if (dto.getAge() == null || dto.getAge() <= 0 || dto.getAge() > 120) {
            return "年龄不合法";
        }
        if (dto.getSex() == null || (!"男".equals(dto.getSex()) && !"女".equals(dto.getSex()))) {
            return "性别必须是男/女";
        }
        if (dto.getClazzName() == null || !clazzNameToId.containsKey(dto.getClazzName())) {
            return "班级不存在：" + dto.getClazzName();
        }
        return null;
    }

    public List<String> getErrors() {
        return errors;
    }

    public int getTotalCount() {
        return cachedDataList.size();
    }
}