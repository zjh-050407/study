package com.ggs.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.ggs.dto.StudentImportDTO;
import com.ggs.pojo.Clazz;
import com.ggs.pojo.Student;
import com.ggs.service.StudentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StudentImportListener implements ReadListener<StudentImportDTO> {

    private static final int BATCH_COUNT = 100;

    private final StudentService studentService;
    private final Map<String, Integer> clazzNameToId;
    private final List<StudentImportDTO> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    private final List<String> errors = new ArrayList<>();
    private int successCount = 0;

    public StudentImportListener(StudentService studentService, List<Clazz> clazzList) {
        this.studentService = studentService;
        this.clazzNameToId = clazzList.stream()
                .collect(Collectors.toMap(Clazz::getName, Clazz::getCid, (a, b) -> a));
    }

    @Override
    public void invoke(StudentImportDTO data, AnalysisContext context) {
        cachedDataList.add(data);
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            cachedDataList.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
    }

    private void saveData() {
        for (int i = 0; i < cachedDataList.size(); i++) {
            StudentImportDTO dto = cachedDataList.get(i);
            String error = validate(dto);
            if (error != null) {
                errors.add("第" + (i + 1) + "行：" + error);
                continue;
            }
            Student student = new Student();
            student.setName(dto.getName());
            student.setAge(dto.getAge());
            student.setSex(dto.getSex());
            student.setCid(clazzNameToId.get(dto.getClazzName()));
            studentService.addStu(student);
            successCount++;
        }
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

    public int getSuccessCount() {
        return successCount;
    }

    public List<String> getErrors() {
        return errors;
    }
}
