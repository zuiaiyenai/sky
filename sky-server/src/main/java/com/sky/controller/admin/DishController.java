package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/dish")
public class DishController {
    @Autowired
    private DishService dishService;
//分页查询菜品
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询，参数：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }
//根据id查询菜品
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品：{}", id);
        return Result.success(dishService.getById(id));
    }
//修改菜品
    @PutMapping
    public Result<Void> update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品，参数：{}", dishDTO);
        dishService.update(dishDTO);
        return Result.success();
    }
//批量删除菜品
    @DeleteMapping
    public Result<Void> batchDelete(@RequestParam List<Long> ids) {
        log.info("批量删除菜品，参数：{}", ids);
        dishService.batchDelete(ids);
        return Result.success();
    }
//新增菜品
    @PostMapping
    public Result<Long> add(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品，参数：{}", dishDTO);
        Long id = dishService.add(dishDTO);
        return Result.success(id);
    }
//根据分类id查询菜品
    @GetMapping("/list")
    public Result<List<DishVO>> getByCategory(Long categoryId) {
        log.info("根据分类id查询菜品：{}", categoryId);
        List<DishVO> dishList = dishService.getByCategoryId(categoryId);
        return Result.success(dishList);
    }
//起售，停售菜品
@PostMapping("/status/{status}")
public Result<Void> startStop(@PathVariable Integer status, @RequestParam Long id) {
    log.info("菜品起售停售，状态：{}，菜品id：{}", status, id);
    dishService.startStop(status, id);
    return Result.success();
}

}
