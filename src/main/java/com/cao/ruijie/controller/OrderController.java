package com.cao.ruijie.controller;


import com.cao.ruijie.common.result.Result;
import com.cao.ruijie.entity.Orders;
import com.cao.ruijie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Orders order){
        log.info("订单信息：{}",order);
        orderService.submit(order);
        return Result.success("成功");
    }
}
