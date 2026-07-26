package com.sky.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.vo.UserReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private static final String WX_LOGIN_URL =
            "https://api.weixin.qq.com/sns/jscode2session";

    private static final String GRANT_TYPE =
            "authorization_code";

    private final WeChatProperties weChatProperties;
    private final UserMapper userMapper;

    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {

        String openid = getOpenid(userLoginDTO.getCode());

        if (openid == null || openid.isBlank()) {
            throw new LoginFailedException(
                    MessageConstant.LOGIN_FAILED
            );
        }

        // 判断用户是否已经注册
        User user = userMapper.getByOpenid(openid);

        if (user == null) {
            // 首次登录，自动注册
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();

            userMapper.insert(user);
        }

        return user;
    }

    private String getOpenid(String code) {

        Map<String, String> params = new HashMap<>();
        params.put("appid", weChatProperties.getAppid());
        params.put("secret", weChatProperties.getSecret());
        params.put("js_code", code);
        params.put("grant_type", GRANT_TYPE);

        String json = HttpClientUtil.doGet(
                WX_LOGIN_URL,
                params
        );

        JSONObject jsonObject =
                JSON.parseObject(json);

        return jsonObject.getString("openid");
    }
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<String> dateList = new ArrayList<>();
        List<String> newUserList = new ArrayList<>();
        List<String> totalUserList = new ArrayList<>();

        LocalDate current = begin;
        while (!current.isAfter(end)) {
            dateList.add(current.toString());

            Map<String, Object> map = new HashMap<>();
            map.put("begin", current.atTime(LocalTime.MIN));
            map.put("end", current.atTime(LocalTime.MAX));
            Integer newUsers = userMapper.countByMap(map);
            newUserList.add(newUsers.toString());

            Map<String, Object> totalMap = new HashMap<>();
            totalMap.put("end", current.atTime(LocalTime.MAX));
            Integer totalUsers = userMapper.countByMap(totalMap);
            totalUserList.add(totalUsers.toString());

            current = current.plusDays(1);
        }

        return UserReportVO.builder()
                .dateList(String.join(",", dateList))
                .newUserList(String.join(",", newUserList))
                .totalUserList(String.join(",", totalUserList))
                .build();
    }
}