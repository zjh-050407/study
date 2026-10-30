package com.ggs.controller;

import com.ggs.pojo.Department;
import com.ggs.service.DepartmentService;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/department")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/list")
    public ResultVO listDepartment() {
        RoleCheckUtil.requireRole("professor", "admin");
        List<Department> list = departmentService.listDepartment();
        return ResultVO.success(list);
    }

    @GetMapping("/{did}")
    public ResultVO getById(@PathVariable Integer did) {
        RoleCheckUtil.requireRole("professor", "admin");
        Department department = departmentService.getById(did);
        if (department == null) {
            return ResultVO.err("院系不存在");
        }
        return ResultVO.success(department);
    }

    @PostMapping
    public ResultVO addDepartment(@RequestBody Department department) {
        RoleCheckUtil.requireRole("professor", "admin");
        departmentService.add(department);
        return ResultVO.success();
    }

    @PutMapping
    public ResultVO updateDepartment(@RequestBody Department department) {
        RoleCheckUtil.requireRole("professor", "admin");
        departmentService.update(department);
        return ResultVO.success();
    }

    @DeleteMapping("/{did}")
    public ResultVO deleteDepartment(@PathVariable Integer did) {
        RoleCheckUtil.requireRole("professor", "admin");
        departmentService.delete(did);
        return ResultVO.success();
    }
}
