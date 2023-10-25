package com.cao.ruijie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cao.ruijie.dto.DishDto;
import com.cao.ruijie.entity.Dish;

public interface DishService extends IService<Dish> {

    void saveWithFlavor(DishDto dishDto);
    DishDto getByIdWithFlavor(Long id);
}
