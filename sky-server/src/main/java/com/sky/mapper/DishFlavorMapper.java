package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    void insert(DishFlavor dishFlavor);

    void deleteByDishId(Long dishId);

    void insertBatch(@Param("flavors") List<DishFlavor> flavors);

    void deleteByDishIds(@Param("dishIds") List<Long> dishIds);

    List<DishFlavor> getByDishId(Long dishId);
}
