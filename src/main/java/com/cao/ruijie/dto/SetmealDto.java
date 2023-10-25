package com.cao.ruijie.dto;

import com.cao.ruijie.entity.Setmeal;
import com.cao.ruijie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
