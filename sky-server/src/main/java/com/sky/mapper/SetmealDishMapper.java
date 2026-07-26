package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SetmealDishMapper {

    void deleteBySetmealId(Long setmealId);

    void insertBatch(@Param("setmealDishes") List<SetmealDish> setmealDishes);

    List<SetmealDish> getBySetmealId(Long setmealId);
//批量删除
    void deleteBySetmealIds(@Param("setmealIds") List<Long> ids);
    Integer countByMap(Map map);
}
