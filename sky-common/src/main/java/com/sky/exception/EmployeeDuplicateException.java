package com.sky.exception;

/**
 * 员工已存在异常
 */
public class EmployeeDuplicateException extends BaseException {

    public EmployeeDuplicateException() {
    }

    public EmployeeDuplicateException(String msg) {
        super(msg);
    }

}
