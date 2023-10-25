package com.cao.ruijie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cao.ruijie.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
