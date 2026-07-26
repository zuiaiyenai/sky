package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<String> dateList = new ArrayList<>();
        List<String> turnoverList = new ArrayList<>();

        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            dateList.add(date.toString());

            Map<String, Object> map = createDateRange(date);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnoverList.add(String.valueOf(turnover == null ? 0.0 : turnover));
        }

        return TurnoverReportVO.builder()
                .dateList(String.join(",", dateList))
                .turnoverList(String.join(",", turnoverList))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<String> dateList = new ArrayList<>();
        List<String> newUserList = new ArrayList<>();
        List<String> totalUserList = new ArrayList<>();

        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            dateList.add(date.toString());

            Integer newUsers = userMapper.countByMap(createDateRange(date));
            newUserList.add(String.valueOf(newUsers));

            Map<String, Object> totalMap = new HashMap<>();
            totalMap.put("end", date.atTime(LocalTime.MAX));
            Integer totalUsers = userMapper.countByMap(totalMap);
            totalUserList.add(String.valueOf(totalUsers));
        }

        return UserReportVO.builder()
                .dateList(String.join(",", dateList))
                .newUserList(String.join(",", newUserList))
                .totalUserList(String.join(",", totalUserList))
                .build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<String> dateList = new ArrayList<>();
        List<String> orderCountList = new ArrayList<>();
        List<String> validOrderCountList = new ArrayList<>();
        int totalOrderCount = 0;
        int validOrderCount = 0;

        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            dateList.add(date.toString());

            Map<String, Object> map = createDateRange(date);
            Integer orderCount = orderMapper.countByMap(map);
            orderCountList.add(String.valueOf(orderCount));
            totalOrderCount += orderCount;

            map.put("status", Orders.COMPLETED);
            Integer validCount = orderMapper.countByMap(map);
            validOrderCountList.add(String.valueOf(validCount));
            validOrderCount += validCount;
        }

        double completionRate = totalOrderCount == 0
                ? 0.0
                : validOrderCount * 1.0 / totalOrderCount;

        return OrderReportVO.builder()
                .dateList(String.join(",", dateList))
                .orderCountList(String.join(",", orderCountList))
                .validOrderCountList(String.join(",", validOrderCountList))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(completionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        List<GoodsSalesDTO> sales = orderMapper.getSalesTop10(
                begin.atTime(LocalTime.MIN),
                end.atTime(LocalTime.MAX));

        String nameList = sales.stream()
                .map(GoodsSalesDTO::getName)
                .collect(Collectors.joining(","));
        String numberList = sales.stream()
                .map(item -> String.valueOf(item.getNumber()))
                .collect(Collectors.joining(","));

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    private Map<String, Object> createDateRange(LocalDate date) {
        Map<String, Object> map = new HashMap<>();
        map.put("begin", date.atTime(LocalTime.MIN));
        map.put("end", date.atTime(LocalTime.MAX));
        return map;
    }
}
