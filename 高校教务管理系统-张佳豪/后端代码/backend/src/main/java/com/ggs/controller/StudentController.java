package com.ggs.controller;

import com.alibaba.excel.EasyExcel;
import com.ggs.annotation.OperationLogAnnotation;
import com.ggs.dto.StudentImportDTO;
import com.ggs.listener.StudentImportListener;
import com.ggs.mapper.ClazzMapper;
import com.ggs.mapper.StudentMapper;
import com.ggs.pojo.Clazz;
import com.ggs.pojo.Student;
import com.ggs.service.StudentService;
import com.ggs.vo.ResultVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class StudentController {

    @Autowired
    private StudentService studentService;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private ClazzMapper clazzMapper;

    @GetMapping("/list")
    public ResultVO listStu(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "5") int size,
                            @RequestParam(required = false) String name,
                            @RequestParam(required = false) String cname) {
        int offset = (page - 1) * size;
        List<Student> list = studentMapper.listStuByPage(offset, size, name, cname);
        int total = studentService.countStu(name, cname);
        return ResultVO.success(new ResultVO.PageResult<>(list, total, page, size));
    }

    @DeleteMapping("/stu")
    public ResultVO Delete(long sid) {
        int isDelete = this.studentService.delete(sid);
        if (isDelete == 0) {
            return ResultVO.err("删除失败");
        }
        return ResultVO.success();
    }

    @GetMapping("/stu")
    public ResultVO getStudentByAge(int age) {
        List<Student> find = this.studentService.getStuByAge(age);
        if (find == null) {
            return ResultVO.err("没有找到");
        }
        return ResultVO.success(find);
    }

    @PutMapping("/stu")
    public ResultVO updateStuAge(long sid, long age) {
        int isUpdate = studentService.updateAge(sid, age);
        if (isUpdate == 0) {
            return ResultVO.err("更新失败");
        }
        return ResultVO.success();
    }

    @PostMapping("/stu")
    public ResultVO addStu(@RequestBody Student student) {
        int isAdd = this.studentService.addStu(student);
        if (isAdd == 0) {
            return ResultVO.err("增加失败");
        }
        return ResultVO.success();
    }

    @PutMapping("/stu2")
    public ResultVO updateStu(@RequestBody Student student) {
        int isUpdate = this.studentService.updateStu(student);
        if (isUpdate == 0) {
            return ResultVO.err("更新失败");
        }
        return ResultVO.success();
    }

    @GetMapping("/stu2")
    public ResultVO getStu(Student student) {
        List<Student> find = this.studentService.getStu(student);
        if (find == null || find.isEmpty()) {
            return ResultVO.err("没有找到");
        }
        return ResultVO.success(find);
    }

    @OperationLogAnnotation("导出学生信息")
    @GetMapping("/student/export")
    public void exportStudent(HttpServletResponse response) throws IOException {
        List<Student> list = studentMapper.listStu();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("学生信息", StandardCharsets.UTF_8).replace("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), Student.class)
                .sheet("学生信息")
                .doWrite(list);
    }

    @OperationLogAnnotation("下载学生导入模板")
    @GetMapping("/student/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("学生导入模板", StandardCharsets.UTF_8).replace("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), StudentImportDTO.class)
                .sheet("模板")
                .doWrite(List.of());
    }

    @OperationLogAnnotation("批量导入学生")
    @PostMapping("/student/import")
    public ResultVO importStudent(@RequestParam("file") MultipartFile file) throws IOException {
        List<Clazz> clazzList = clazzMapper.listClazz();
        StudentImportListener listener = new StudentImportListener(studentService, clazzList);
        EasyExcel.read(file.getInputStream(), StudentImportDTO.class, listener).sheet().doRead();
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", listener.getSuccessCount());
        result.put("errorCount", listener.getErrors().size());
        result.put("errors", listener.getErrors());
        return ResultVO.success(result);
    }
}
