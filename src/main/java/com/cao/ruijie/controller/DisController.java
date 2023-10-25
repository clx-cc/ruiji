package com.cao.ruijie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cao.ruijie.common.result.Result;
import com.cao.ruijie.dto.DishDto;
import com.cao.ruijie.entity.Category;
import com.cao.ruijie.entity.Dish;
import com.cao.ruijie.entity.DishFlavor;
import com.cao.ruijie.service.DishFlavorService;
import com.cao.ruijie.service.DishService;
import com.fasterxml.jackson.databind.util.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DisController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    public Result<String> save(@RequestBody DishDto dishDto){
        log.info("dishDto:{}",dishDto);
        dishService.saveWithFlavor(dishDto);
        //清理部分菜品数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        return Result.success("新增菜品成功");
    }

    @GetMapping("/page")
    public Result<Page> page(int page,int pageSize,String name){
        //构造分页构造器
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null,Dish::getName,name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行查询
        dishService.page(pageInfo,queryWrapper);
//        //对象拷贝
//        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
//        List<Dish> records = pageInfo.getRecords();
//        List<DishDto> list = null;
//
//        dishDtoPage.setRecords(list);
        return Result.success(pageInfo);
    }

    @GetMapping("/{id}")
    public Result<DishDto> getById(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return Result.success(dishDto);
    }
    @PutMapping
    public Result<String> update(@RequestBody DishDto dishDto){
        log.info("修改菜品和口味:{}",dishDto);
//        dishService.saveWithFlavor(dishDto);
        //清理所有的菜品数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return null;
    }
    /*@GetMapping("/list")
    public Result<List<Dish>> list(Dish dish){
        //构建查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);
        return Result.success(list);
    }*/
    @GetMapping("/list")
    public Result<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtoList = null;
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        //先尝试从 Redis 中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //如果存在，直接返回，不用查询数据库
        if (dishDtoList != null){
            return Result.success(dishDtoList);
        }
        //构建查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);
        dishDtoList = new ArrayList<>();
        for (Dish item : list) {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> qw = new LambdaQueryWrapper<>();
            qw.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(qw);
            dishDto.setFlavors(dishFlavorList);
            dishDtoList.add(dishDto);
        }
        //如果不存在，需要查询数据库，将查询到的菜品数据缓存到 Redis
        redisTemplate.opsForValue().set(key,dishDtoList,60l, TimeUnit.MINUTES);
        return Result.success(dishDtoList);
    }
}
