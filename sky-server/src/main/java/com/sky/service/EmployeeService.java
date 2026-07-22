package com.sky.service;

import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);
//新增员工
    void save(Employee employee);
//员工分页查询
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);
//修改密码
    void editPassword(PasswordEditDTO passwordEditDTO);
//启用禁用员工账号
    void startOrStop(Integer status, Long id);
//查询员工信息
    Employee getById(Long id);
//修改员工信息
    void update(Employee employee);
}
