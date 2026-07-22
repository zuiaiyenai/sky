package com.sky.handler;

import com.sky.exception.BaseException;
import com.sky.exception.EmployeeDuplicateException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获SQL唯一键约束异常（员工已存在）
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result handleDuplicateKeyException(DuplicateKeyException ex) {
        log.error("数据库唯一键冲突：{}", ex.getMessage());

        String message = "操作失败";

        if (ex.getMessage() != null && ex.getMessage().contains("idx_username")) {
            message = "员工账号已存在";
        } else if (ex.getMessage() != null && ex.getMessage().contains("idx_phone")) {
            message = "手机号已存在";
        } else {
            message = "数据插入失败，请检查输入数据";
        }

        return Result.error(message);
    }

}
