package com.sky.mapper;

import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);
//分页查询菜品
    List<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);
//查询总记录数
    Long pageQueryCount(DishPageQueryDTO dishPageQueryDTO);
//修改菜品
    void update(Dish dish);
//根据id查询菜品
    Dish getById(Long id);
//批量删除菜品
    void deleteByIds(List<Long> ids);
//查询菜品和口味
    List<Dish> list(Dish dish);
//新增菜品
    void insert(Dish dish);
//根据分类id查询菜品
    List<DishVO> getByCategoryId(Long categoryId);
    /**
     * 根据条件统计菜品数量
     * @param dish
     * @return
     */
    Integer countByMap(Map map);

}
