package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.BaseException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private com.sky.mapper.DishMapper dishMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    //分页查询
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("套餐分页查询参数：{}", setmealPageQueryDTO);

        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        Page<SetmealVO> page = (Page<SetmealVO>) setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    //修改套餐
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        if (setmealDTO.getId() == null) {
            throw new BaseException("套餐id不能为空");
        }
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmeal.setUpdateTime(LocalDateTime.now());
        setmeal.setUpdateUser(BaseContext.getCurrentId());
        setmealMapper.update(setmeal);
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(dish -> dish.setSetmealId(setmealDTO.getId()));
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    //套餐起售，停售
    @Override
    public void startStop(Integer status, Long id) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BaseException("套餐状态参数不正确");
        }
        if (id == null) {
            throw new BaseException("套餐id不能为空");
        }

        Setmeal setmeal = setmealMapper.getById(id);
        if (setmeal == null) {
            throw new BaseException("套餐不存在");
        }

        if (status == 1) {
            List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
            if (setmealDishes != null && !setmealDishes.isEmpty()) {
                for (SetmealDish setmealDish : setmealDishes) {
                    com.sky.entity.Dish dish = dishMapper.getById(setmealDish.getDishId());
                    if (dish == null || dish.getStatus() != 1) {
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                }
            }
        }
        setmeal.setStatus(status);
        setmeal.setUpdateTime(LocalDateTime.now());
        setmeal.setUpdateUser(BaseContext.getCurrentId());
        setmealMapper.update(setmeal);
    }

    //删除套餐
    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BaseException("请选择要删除的套餐");
        }
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal == null) {
                throw new BaseException("套餐不存在：" + id);
            }
            if (setmeal.getStatus() == 1) {
                throw new BaseException("套餐 \"" + setmeal.getName() + "\" 正在售卖中，无法删除");
            }
        }
        setmealDishMapper.deleteBySetmealIds(ids);
        setmealMapper.deleteByIds(ids);
    }

    //新增套餐
    @Override
    @Transactional
    public Long add(SetmealDTO setmealDTO) {
        if (setmealDTO.getName() == null || setmealDTO.getName().trim().isEmpty()) {
            throw new BaseException("套餐名称不能为空");
        }
        if (setmealDTO.getCategoryId() == null) {
            throw new BaseException("套餐分类不能为空");
        }
        if (setmealDTO.getPrice() == null) {
            throw new BaseException("套餐价格不能为空");
        }
        if (setmealDTO.getImage() == null || setmealDTO.getImage().trim().isEmpty()) {
            throw new BaseException("套餐图片不能为空");
        }
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes == null || setmealDishes.isEmpty()) {
            throw new BaseException("套餐菜品不能为空");
        }

        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmeal.setStatus(setmealDTO.getStatus() != null ? setmealDTO.getStatus() : 1);
        setmeal.setCreateTime(LocalDateTime.now());
        setmeal.setUpdateTime(LocalDateTime.now());
        setmeal.setCreateUser(BaseContext.getCurrentId());
        setmeal.setUpdateUser(BaseContext.getCurrentId());
        setmealMapper.insert(setmeal);
        setmealDishes.forEach(dish -> dish.setSetmealId(setmeal.getId()));
        setmealDishMapper.insertBatch(setmealDishes);
        return setmeal.getId();
    }

    //根据id查询套餐
    @Override
    public SetmealVO getByIdWithDishes(Long id) {
        if (id == null) {
            throw new BaseException("套餐id不能为空");
        }
        Setmeal setmeal = setmealMapper.getById(id);
        if (setmeal == null) {
            throw new BaseException("套餐不存在");
        }
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        com.sky.entity.Category category = categoryMapper.getById(setmeal.getCategoryId());
        if (category != null) {
            setmealVO.setCategoryName(category.getName());
        }
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }
}
