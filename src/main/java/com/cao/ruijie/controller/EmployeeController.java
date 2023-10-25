package com.cao.ruijie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cao.ruijie.common.result.Result;
import com.cao.ruijie.entity.Employee;
import com.cao.ruijie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @PostMapping(value = {"/login"})
    public Result<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //MD5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //根据用户名查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        //如果没有查询到返回登录失败
        if (emp == null){
            return Result.error("用户不存在！");
        }
        //密码比对
        if (!emp.getPassword().equals(password)){
            return Result.error("密码不正确！");
        }
        //查看员工状态
        if (emp.getStatus() == 0){
            return Result.error("账号已禁用!");
        }
        //登录成功，将员工ID放入 session 中
        request.getSession().setAttribute("employee",emp.getId());
        return Result.success(emp);
    }
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return Result.success("退出成功！");
    }
    @PostMapping
    public Result<String> save(HttpSession session, @RequestBody Employee employee){
        log.info("新增员工{}",employee);
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());

//        Long createUserId = (Long) session.getAttribute("employee");
//        employee.setCreateUser(createUserId);
//        employee.setUpdateUser(createUserId);
        employeeService.save(employee);
        return Result.success("新增员工成功");
    }
    @GetMapping("/page")
    public Result<Page> page(int page,int pageSize,String name){
        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);//添加过滤条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);//添加排序条件
        //执行查询
        employeeService.page(pageInfo,queryWrapper);
        return Result.success(pageInfo);
    }
    @PutMapping
    public Result<String> update(HttpSession session,@RequestBody Employee employee){
        log.info("员工信息：{}",employee);
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser((Long) session.getAttribute("employee"));
        employeeService.updateById(employee);
        return Result.success("更新成功！");
    }
    @GetMapping("/{id}")
    public Result<Employee> selectById(@PathVariable Long id){
        log.info("通过 id 查询员工信息...");
        Employee emp = employeeService.getById(id);
        if (emp != null){
            return Result.success(emp);
        }
        return Result.error("没有找到用户");

    }
}
