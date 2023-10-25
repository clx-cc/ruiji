package com.cao.ruijie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cao.ruijie.common.result.Result;
import com.cao.ruijie.dto.SetmealDto;
import com.cao.ruijie.entity.Category;
import com.cao.ruijie.entity.Setmeal;
import com.cao.ruijie.entity.SetmealDish;
import com.cao.ruijie.service.CategoryService;
import com.cao.ruijie.service.SetmealDishService;
import com.cao.ruijie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/setmeal")
@CacheConfig(cacheNames = "setmealCache")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    @CacheEvict(allEntries = true)
    @PostMapping
    public Result<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息：{}",setmealDto);
        setmealService.saveWithDish(setmealDto);
        return Result.success("套餐添加成功");
    }
    @GetMapping("/page")
    public Result<Page> page(int page,int pageSize,String name){
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name !=null,Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo,queryWrapper);
        //拷贝
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        List<Setmeal> setmeals = pageInfo.getRecords();
        List<SetmealDto> setmealDtos = new ArrayList<>();
        for (int i = 0; i < setmeals.size(); i++) {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(setmeals.get(i),setmealDto);
            //对象拷贝
            Long categoryId = setmeals.get(i).getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null){
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            setmealDtos.add(setmealDto);
        }
        dtoPage.setRecords(setmealDtos);
        return Result.success(dtoPage);
    }

    //清理 setmealCache 下的所有缓存数据
    @CacheEvict(allEntries = true)
    @DeleteMapping
    public Result<String> delete(@RequestParam("ids") List<Long> ids){
        log.info("删除套餐信息：{}",ids);
        setmealService.removeWithDish(ids);
        return Result.success("删除成功");
    }

    @Cacheable(key = "#setmeal.categoryId+'_'+#setmeal.status")
    @GetMapping("/list")
    public Result<List<Setmeal>> list(Setmeal setmeal){
        log.info("套餐信息，setmeal：{}",setmeal);
        LambdaQueryWrapper<Setmeal> qw = new LambdaQueryWrapper<>();
        qw.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        qw.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        List<Setmeal> list = setmealService.list(qw);
        return Result.success(list);
    }
    /*@GetMapping("/dish")
    public Result<List<SetmealDish>> dish(Setmeal setmeal){
        log.info("setmeal:{}",setmeal);

        return null;
    }*/
}
