package com.ggs.controller;

import com.ggs.pojo.Clazz;
import com.ggs.service.ClazzService;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clazz")
public class ClazzController {

    @Autowired
    private ClazzService clazzService;

    @GetMapping("/list")
    public ResultVO listClazz() {
        List<Clazz> list = clazzService.listClazz();
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
        clazzService.delete(cid);
        return ResultVO.success();
    }

    @GetMapping("/dept/{did}")
    public ResultVO listByDept(@PathVariable Integer did) {
        List<Clazz> list = clazzService.listByDept(did);
        return ResultVO.success(list);
    }
}
