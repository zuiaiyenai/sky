package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordEditFailedException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.sky.constant.StatusConstant.ENABLE;
@Slf4j
@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;
//登录
    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();
        Employee employee = employeeMapper.getByUsername(username);
        if (employee == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!encryptedPassword.equals(employee.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        if (employee.getStatus() != null && employee.getStatus() == StatusConstant.DISABLE) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }
        return employee;
    }
//新增员工
    @Override
    public void save(Employee employee) {
        String defaultPassword = DigestUtils.md5DigestAsHex(
                ("123456").getBytes());
        employee.setPassword(defaultPassword);
        employee.setStatus(StatusConstant.ENABLE);
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.insert(employee);
    }
    //分页查询
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        long total = page.getTotal();
        List<Employee> records = page.getResult();
        return new PageResult(total, records);
    }
//修改密码
@Override
public void editPassword(PasswordEditDTO passwordEditDTO) {
    Long empId = passwordEditDTO.getEmpId();
    if (empId == null) {
        throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
    }
    String oldPassword = passwordEditDTO.getOldPassword();
    String newPassword = passwordEditDTO.getNewPassword();
    Employee employee = employeeMapper.getById(empId);
    String encryptedOldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
    if (!encryptedOldPassword.equals(employee.getPassword())) {
        throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
    }
    if (oldPassword.equals(newPassword)) {
        throw new PasswordEditFailedException("新密码不能与旧密码相同");
    }
    Employee employeeUpdate = new Employee();
    employeeUpdate.setId(empId);
    employeeUpdate.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
    employeeMapper.updatePassword(employeeUpdate);
    log.info("员工修改密码成功，员工ID：{}", empId);
}
//禁用员工账号
    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .updateTime(LocalDateTime.now())
                .build();
        employeeMapper.update(employee);
    }
//根据id查询 员工
    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("****");
        return employee;
    }
    //修改员工信息

    @Override
    public void update(Employee employee) {
        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.update(employee);
        log.info("员工信息修改成功，员工ID：{}", employee.getId());
    }

}
