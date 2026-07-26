package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface UserService {
    User wxLogin(UserLoginDTO userLoginDTO);

    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);
}
