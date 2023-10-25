package com.cao.ruijie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cao.ruijie.entity.Orders;

public interface OrderService extends IService<Orders> {

    /**
     * 用户下单
     * @param order
     */
    void submit(Orders order);
}
