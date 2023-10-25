package com.cao.ruijie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cao.ruijie.entity.DishFlavor;
import com.cao.ruijie.mapper.DishFlavorMappper;
import com.cao.ruijie.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMappper, DishFlavor>implements DishFlavorService {
}
