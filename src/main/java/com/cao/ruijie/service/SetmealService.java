package com.cao.ruijie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cao.ruijie.dto.SetmealDto;
import com.cao.ruijie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐
     * @param setmealDto
     */
    void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐
     * @param ids
     */
    void removeWithDish(List<Long> ids);
}
