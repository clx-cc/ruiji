package com.cao.ruijie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cao.ruijie.common.result.Result;
import com.cao.ruijie.entity.ShoppingCart;
import com.cao.ruijie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public Result<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart, HttpSession session){
        log.info("购物车信息，shoppingCart:{}",shoppingCart);
        //设置用户id，指定当前是哪个用户的购物车数据
        shoppingCart.setUserId((Long) session.getAttribute("user"));
        //查询当前菜品或套餐是否在购物车里
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
        qw.eq(ShoppingCart::getUserId,shoppingCart.getUserId());

        if (dishId != null){
            //添加到购物车里的是菜品
            qw.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
//            qw.eq(ShoppingCart::getDishFlavor,shoppingCart.getDishFlavor());

        }else{
            //添加到购物车里的是套餐
            qw.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        ShoppingCart cartServiceOne = shoppingCartService.getOne(qw);
        //如果已经存在，数量加1
        if (cartServiceOne != null){
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
            return Result.success(cartServiceOne);
        }
        //如果不存在，添加到购物车，数量为1
        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartService.save(shoppingCart);
        cartServiceOne = shoppingCart;
        return Result.success(cartServiceOne);
    }
    @PostMapping("/sub")
    public Result<String> sub(@RequestBody ShoppingCart shoppingCart,HttpSession session){
        log.info("减少菜品：{}",shoppingCart);
        shoppingCart.setUserId((Long) session.getAttribute("user"));
        LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
        qw.eq(ShoppingCart::getUserId,shoppingCart.getUserId());
        Long dishId = shoppingCart.getDishId();
        if (dishId != null){
            qw.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else{
            qw.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        ShoppingCart cartServiceOne = shoppingCartService.getOne(qw);
        Integer number = cartServiceOne.getNumber();
        if (number == 1){
            shoppingCartService.removeById(cartServiceOne);
        }else{
            cartServiceOne.setNumber(number - 1);
            shoppingCartService.updateById(cartServiceOne);
        }
        return Result.success("成功");
    }
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list(HttpSession session){
        log.info("查询购物车");
        LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
        qw.eq(ShoppingCart::getUserId,session.getAttribute("user"));
        qw.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(qw);
        return Result.success(list);
    }
    @DeleteMapping("/clean")
    public Result<String> clean(HttpSession session){
        LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
        qw.eq(ShoppingCart::getUserId,session.getAttribute("user"));
        shoppingCartService.remove(qw);
        return Result.success("清空购物车成功");
    }
}
