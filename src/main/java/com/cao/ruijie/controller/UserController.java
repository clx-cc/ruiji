package com.cao.ruijie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cao.ruijie.common.result.Result;
import com.cao.ruijie.common.utils.SMSUtils;
import com.cao.ruijie.common.utils.ValidateCodeUtils;
import com.cao.ruijie.entity.User;
import com.cao.ruijie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送手机验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public Result<String> sendMsg(@RequestBody User user, HttpSession session){
        log.info("接收到的user信息：{}",user);
        //获取手机号
        if (StringUtils.isNotEmpty(user.getPhone())){

            //生成随机验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code:{}",code);
            //阿里云发送短信
            log.info("发送短信...");
            //保存验证码到session
            //session.setAttribute(user.getPhone(),code);

            //将生成的验证码缓存到 Redis 中，并且设置有效其为 5 分钟
            redisTemplate.opsForValue().set(user.getPhone(),code,5l, TimeUnit.MINUTES);
            return Result.success("短信发送成功");
        }

        return Result.error("短信发送失败");
    }

    /**
     * 移动端用户登录
     * @return
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody Map map,HttpSession session){
        log.info("移动端登录请求：{}",map);
        //手机号
        String phone = (String) map.get("phone");
        //验证码
        String code = (String) map.get("code");
        //从session中获取保存的验证码
        //String codeInSession = (String) session.getAttribute(phone);

        //从 Redis 中获取缓存的验证码
        String codeInRedis = (String) redisTemplate.opsForValue().get(phone);

        //比对两个验证码，如果相等，登录成功,否则不能登录
        if (!(codeInRedis !=null && code.equals(codeInRedis))){
            return Result.error("验证码不正确！登录失败");
        }
        //判断是否是新用户，如果是新用户则自动注册，否则直接登录
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone,phone);
        User user = userService.getOne(queryWrapper);
        if (user == null){
            user = new User();
            user.setPhone(phone);
            user.setStatus(1);
            userService.save(user);
        }
        //保存user的id
        session.setAttribute("user",user.getId());

        //登录成功，将验证码从缓存中删除
        redisTemplate.delete(phone);

        return Result.success(user);
    }
}
