package com.cao.ruijie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cao.ruijie.entity.Category;

public interface CategoryService extends IService<Category> {

    void remove(Long id);
}
