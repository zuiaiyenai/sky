package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    void insert(Orders order);

    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    void update(Orders orders);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select count(id) from orders where status = #{status}")
    Integer countByStatus(Integer status);

    /**
     * 根据时间范围、订单状态等条件统计订单数量
     */
    Integer countByMap(Map map);

    /**
     * 根据时间范围、订单状态等条件统计订单金额
     */
    Double sumByMap(Map map);

    /**
     * 查询指定时间范围内已完成订单的商品销量前十
     */
    List<GoodsSalesDTO> getSalesTop10(@Param("begin") LocalDateTime begin,
                                     @Param("end") LocalDateTime end);

}
