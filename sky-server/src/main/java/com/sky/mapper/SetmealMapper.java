package com.sky.mapper;

import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
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
}
