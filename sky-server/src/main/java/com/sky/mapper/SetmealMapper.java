package com.sky.mapper;

import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SetmealMapper {

    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    List<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void update(Setmeal setmeal);

    Setmeal getById(Long id);
//批量删除
    void deleteByIds(@Param("ids") List<Long> ids);
//新增套餐
    void insert(Setmeal setmeal);

    /**
     * 动态条件查询套餐
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐 id 查询菜品选项
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);
}
