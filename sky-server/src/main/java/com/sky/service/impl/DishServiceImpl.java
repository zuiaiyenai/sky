package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.BaseException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;
//分页查询菜品
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = (Page<DishVO>) dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }
//根据id查询菜品
    @Override
    public DishVO getById(Long id) {
        Dish dish = dishMapper.getById(id);
        if (dish == null) {
            throw new BaseException("菜品不存在");
        }
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavorMapper.getByDishId(id));
        return dishVO;
    }
//修改菜品
    @Override
    @Transactional
    public void update(DishDTO dishDTO) {
        if (dishDTO.getId() == null) {
            throw new BaseException("菜品id不能为空");
        }

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(BaseContext.getCurrentId());
        dishMapper.update(dish);

        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> flavor.setDishId(dishDTO.getId()));
            dishFlavorMapper.insertBatch(flavors);
        }
    }
    //批量删除菜品
    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BaseException("请选择要删除的菜品");
        }

        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish == null) {
                throw new BaseException("菜品不存在：" + id);
            }
            if (dish.getStatus() == 1) {
                throw new BaseException("菜品 \"" + dish.getName() + "\" 正在售卖中，无法删除");
            }
        }

        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }
    //新增菜品
    @Override
    @Transactional
    public Long add(DishDTO dishDTO) {
        if (dishDTO.getName() == null || dishDTO.getName().trim().isEmpty()) {
            throw new BaseException("菜品名称不能为空");
        }
        if (dishDTO.getCategoryId() == null) {
            throw new BaseException("菜品分类不能为空");
        }
        if (dishDTO.getPrice() == null) {
            throw new BaseException("菜品价格不能为空");
        }

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setStatus(dishDTO.getStatus() != null ? dishDTO.getStatus() : 1);
        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateTime(LocalDateTime.now());
        dish.setCreateUser(BaseContext.getCurrentId());
        dish.setUpdateUser(BaseContext.getCurrentId());
        dishMapper.insert(dish);

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> flavor.setDishId(dish.getId()));
            dishFlavorMapper.insertBatch(flavors);
        }

        return null;
    }
//根据分类id查询菜品
    @Override
    public List<DishVO> getByCategoryId(Long categoryId) {
        if (categoryId == null) {
            throw new BaseException("分类id不能为空");
        }
        return dishMapper.getByCategoryId(categoryId);
    }
    //起售、停售
    @Override
    public void startStop(Integer status, Long id) {
        if (status == null || status != 0 && status != 1) {
            throw new BaseException("菜品状态参数不正确");
        }
        if (id == null) {
            throw new BaseException("菜品id不能为空");
        }

        Dish dish = dishMapper.getById(id);
        if (dish == null) {
            throw new BaseException("菜品不存在");
        }

        dish.setStatus(status);
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(BaseContext.getCurrentId());
        dishMapper.update(dish);
    }
}
