package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.entity.Employee;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.annotation.AutoFill;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface EmployeeMapper {

    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);

    @AutoFill(OperationType.INSERT)
    @Insert("INSERT INTO employee (" +
            "username, name, password, phone, sex, id_number, status, " +
            "create_time, update_time, create_user, update_user" +
            ") VALUES (" +
            "#{username}, #{name}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, " +
            "#{createTime}, #{updateTime}, #{createUser}, #{updateUser}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Employee employee);

    @Update("UPDATE employee SET password = #{password}, update_time = NOW() WHERE id = #{id}")
    void updatePassword(Employee employee);

    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    @AutoFill(OperationType.UPDATE)
    void update(Employee employee);
}
