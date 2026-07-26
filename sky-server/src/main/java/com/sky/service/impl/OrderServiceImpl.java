package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;

    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        Long userId = BaseContext.getCurrentId();
        ShoppingCart query = new ShoppingCart();
        query.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(query);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, order);
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setUserId(userId);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPayStatus(Orders.UN_PAID);
        order.setOrderTime(LocalDateTime.now());
        orderMapper.insert(order);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(order.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        shoppingCartMapper.deleteByUserId(userId);

        return OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();
    }

    @Override
    @Transactional
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) {
        Orders order = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber());
        if (order == null) {
            throw new OrderBusinessException("订单不存在");
        }
        checkCurrentUser(order);

        if (Orders.PAID.equals(order.getPayStatus())) {
            return OrderPaymentVO.builder().build();
        }
        if (!Orders.PENDING_PAYMENT.equals(order.getStatus())) {
            throw new OrderBusinessException("订单状态错误，无法支付");
        }

        Orders paidOrder = Orders.builder()
                .id(order.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .payMethod(ordersPaymentDTO.getPayMethod())
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.update(paidOrder);
        log.info("订单直接支付成功，订单号：{}", order.getNumber());
        return OrderPaymentVO.builder().build();
    }

    @Override
    @Transactional
    public void paySuccess(String outTradeNo) {
        Orders order = orderMapper.getByNumber(outTradeNo);
        if (order == null) {
            throw new OrderBusinessException("订单不存在");
        }
        if (Orders.PAID.equals(order.getPayStatus())) {
            return;
        }
        Orders paidOrder = Orders.builder()
                .id(order.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.update(paidOrder);
    }

    @Override
    public PageResult pageQueryForUser(int page, int pageSize, Integer status) {
        validatePage(page, pageSize);
        OrdersPageQueryDTO query = new OrdersPageQueryDTO();
        query.setPage(page);
        query.setPageSize(pageSize);
        query.setStatus(status);
        query.setUserId(BaseContext.getCurrentId());

        PageHelper.startPage(page, pageSize);
        Page<Orders> ordersPage = orderMapper.pageQuery(query);
        List<OrderVO> records = ordersPage.getResult().stream()
                .map(this::buildOrderVO)
                .collect(Collectors.toList());
        return new PageResult(ordersPage.getTotal(), records);
    }

    @Override
    public OrderVO userOrderDetail(Long id) {
        Orders order = getOrderById(id);
        checkCurrentUser(order);
        return buildOrderVO(order);
    }

    @Override
    @Transactional
    public void userCancelById(Long id) {
        Orders order = getOrderById(id);
        checkCurrentUser(order);
        if (!Orders.PENDING_PAYMENT.equals(order.getStatus())
                && !Orders.TO_BE_CONFIRMED.equals(order.getStatus())) {
            throw new OrderBusinessException("订单状态错误，不能取消");
        }

        Orders cancelledOrder = Orders.builder()
                .id(id)
                .status(Orders.CANCELLED)
                .cancelReason("用户取消")
                .cancelTime(LocalDateTime.now())
                .payStatus(Orders.PAID.equals(order.getPayStatus()) ? Orders.REFUND : Orders.UN_PAID)
                .build();
        orderMapper.update(cancelledOrder);
    }

    @Override
    @Transactional
    public void repetition(Long id) {
        Orders order = getOrderById(id);
        checkCurrentUser(order);
        List<OrderDetail> details = orderDetailMapper.getByOrderId(id);
        if (details == null || details.isEmpty()) {
            throw new OrderBusinessException("订单明细不存在");
        }

        Long userId = BaseContext.getCurrentId();
        LocalDateTime now = LocalDateTime.now();
        for (OrderDetail detail : details) {
            ShoppingCart cart = ShoppingCart.builder()
                    .name(detail.getName())
                    .userId(userId)
                    .dishId(detail.getDishId())
                    .setmealId(detail.getSetmealId())
                    .dishFlavor(detail.getDishFlavor())
                    .number(detail.getNumber())
                    .amount(detail.getAmount())
                    .image(detail.getImage())
                    .createTime(now)
                    .build();
            shoppingCartMapper.insert(cart);
        }
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        validatePage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> ordersPage = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> records = ordersPage.getResult().stream()
                .map(this::buildOrderVO)
                .collect(Collectors.toList());
        return new PageResult(ordersPage.getTotal(), records);
    }

    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO statistics = new OrderStatisticsVO();
        statistics.setToBeConfirmed(orderMapper.countByStatus(Orders.TO_BE_CONFIRMED));
        statistics.setConfirmed(orderMapper.countByStatus(Orders.CONFIRMED));
        statistics.setDeliveryInProgress(orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS));
        return statistics;
    }

    @Override
    public OrderVO orderDetail(Long id) {
        return buildOrderVO(getOrderById(id));
    }

    @Override
    @Transactional
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders order = getOrderById(ordersConfirmDTO.getId());
        requireStatus(order, Orders.TO_BE_CONFIRMED, "订单状态错误，不能接单");
        updateStatus(order.getId(), Orders.CONFIRMED);
    }

    @Override
    @Transactional
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders order = getOrderById(ordersRejectionDTO.getId());
        requireStatus(order, Orders.TO_BE_CONFIRMED, "订单状态错误，不能拒单");

        Orders rejectedOrder = Orders.builder()
                .id(order.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .payStatus(Orders.PAID.equals(order.getPayStatus()) ? Orders.REFUND : Orders.UN_PAID)
                .build();
        orderMapper.update(rejectedOrder);
    }

    @Override
    @Transactional
    public void adminCancel(OrdersCancelDTO ordersCancelDTO) {
        Orders order = getOrderById(ordersCancelDTO.getId());
        if (Orders.CANCELLED.equals(order.getStatus()) || Orders.COMPLETED.equals(order.getStatus())) {
            throw new OrderBusinessException("订单状态错误，不能取消");
        }

        Orders cancelledOrder = Orders.builder()
                .id(order.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .payStatus(Orders.PAID.equals(order.getPayStatus()) ? Orders.REFUND : Orders.UN_PAID)
                .build();
        orderMapper.update(cancelledOrder);
    }

    @Override
    @Transactional
    public void delivery(Long id) {
        Orders order = getOrderById(id);
        requireStatus(order, Orders.CONFIRMED, "订单状态错误，不能派送");
        updateStatus(id, Orders.DELIVERY_IN_PROGRESS);
    }

    @Override
    @Transactional
    public void complete(Long id) {
        Orders order = getOrderById(id);
        requireStatus(order, Orders.DELIVERY_IN_PROGRESS, "订单状态错误，不能完成");
        Orders completedOrder = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();
        orderMapper.update(completedOrder);
    }

    private Orders getOrderById(Long id) {
        Orders order = orderMapper.getById(id);
        if (order == null) {
            throw new OrderBusinessException("订单不存在");
        }
        return order;
    }

    private void checkCurrentUser(Orders order) {
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null || !currentUserId.equals(order.getUserId())) {
            throw new OrderBusinessException("无权操作该订单");
        }
    }

    private void requireStatus(Orders order, Integer requiredStatus, String message) {
        if (!requiredStatus.equals(order.getStatus())) {
            throw new OrderBusinessException(message);
        }
    }

    private void updateStatus(Long id, Integer status) {
        orderMapper.update(Orders.builder().id(id).status(status).build());
    }

    private void validatePage(int page, int pageSize) {
        if (page < 1 || pageSize < 1) {
            throw new OrderBusinessException("分页参数必须大于0");
        }
    }

    private OrderVO buildOrderVO(Orders order) {
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        List<OrderDetail> details = orderDetailMapper.getByOrderId(order.getId());
        orderVO.setOrderDetailList(details);
        orderVO.setOrderDishes(buildOrderDishes(details));
        return orderVO;
    }

    private String buildOrderDishes(List<OrderDetail> details) {
        if (details == null || details.isEmpty()) {
            return "";
        }
        return details.stream()
                .map(detail -> detail.getName() + "*" + detail.getNumber())
                .collect(Collectors.joining(";"));
    }
}
