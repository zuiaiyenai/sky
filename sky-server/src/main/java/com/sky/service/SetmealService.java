package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    //套餐分页查询
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);
//修改套餐
    void update(SetmealDTO setmealDTO);
    //套餐起售、停售
    void startStop(Integer status, Long id);
//批量删除套餐
    void batchDelete(List<Long> ids);
//新增套餐
    Long add(SetmealDTO setmealDTO);
//根基id查询套餐
    SetmealVO getByIdWithDishes(Long id);
}