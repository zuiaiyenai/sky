package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    //菜品查询
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    //根据id查询菜品及口味
    DishVO getById(Long id);

    //修改菜品
    void update(DishDTO dishDTO);
    //批量删除菜品
    void batchDelete(List<Long> ids);
//添加菜品
    Long add(DishDTO dishDTO);
//根据分类id查询菜品
    List<DishVO> getByCategoryId(Long categoryId);
    //起售、停售
    void startStop(Integer status, Long id);

}
