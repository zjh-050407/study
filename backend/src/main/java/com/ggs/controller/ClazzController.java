package com.ggs.controller;

import com.ggs.mapper.ClazzMapper;
import com.ggs.pojo.Clazz;
import com.ggs.service.ClazzService;
import com.ggs.util.DataScopeUtil;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clazz")
public class ClazzController {

    @Autowired
    private ClazzService clazzService;

    @Autowired
    private ClazzMapper clazzMapper;

    @GetMapping("/list")
    public ResultVO listClazz(@RequestParam(required = false) Integer page,
                              @RequestParam(required = false) Integer size) {
        Integer did = DataScopeUtil.currentDid();
        if (page != null && size != null) {
            int offset = (page - 1) * size;
            List<Clazz> list = clazzMapper.listByPage(offset, size, did);
            int total = clazzMapper.countAll(did);
            return ResultVO.success(new ResultVO.PageResult<>(list, total, page, size));
        }
        List<Clazz> list = clazzMapper.listClazz(did);
        return ResultVO.success(list);
    }

    @GetMapping("/{cid}")
    public ResultVO getById(@PathVariable Integer cid) {
        Clazz clazz = clazzService.getById(cid);
        if (clazz == null) {
            return ResultVO.err("班级不存在");
        }
        return ResultVO.success(clazz);
    }

    @PostMapping
    public ResultVO addClazz(@RequestBody Clazz clazz) {
        clazzService.add(clazz);
        return ResultVO.success();
    }

    @PutMapping
    public ResultVO updateClazz(@RequestBody Clazz clazz) {
        clazzService.update(clazz);
        return ResultVO.success();
    }

    @DeleteMapping("/{cid}")
    public ResultVO deleteClazz(@PathVariable Integer cid) {
        RoleCheckUtil.requireRole("professor", "admin");
        clazzService.delete(cid);
        return ResultVO.success();
    }

    @GetMapping("/dept/{did}")
    public ResultVO listByDept(@PathVariable Integer did) {
        List<Clazz> list = clazzService.listByDept(did);
        return ResultVO.success(list);
    }
}
