package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("admin")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {

    private static final String SHOP_STATUS_KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置店铺营业状态
     * status：1 营业，0 打烊
     */
    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result<String> setStatus(
            @PathVariable("status") Integer status) {

        log.info("设置店铺营业状态：{}", status);

        redisTemplate.opsForValue().set(SHOP_STATUS_KEY, status);

        return Result.success();
    }

    /**
     * 获取店铺营业状态
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result<Integer> getStatus() {

        Integer status =
                (Integer) redisTemplate.opsForValue().get(SHOP_STATUS_KEY);

        log.info("获取店铺营业状态：{}", status);

        return Result.success(status);
    }
}