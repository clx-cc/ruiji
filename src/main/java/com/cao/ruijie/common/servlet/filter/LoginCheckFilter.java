package com.cao.ruijie.common.servlet.filter;

import com.alibaba.fastjson.JSON;
import com.cao.ruijie.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        log.info("拦截到请求：{}",request.getRequestURI());
        //获取请求 URL
        String requestURI = request.getRequestURI();
        //定义不需要拦截的请求
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login"
        };
        boolean check = check(urls,requestURI);
        if (check){//不需要处理，直接放行
            log.info("本次请求{}不需要处理",request.getRequestURI());
            chain.doFilter(request,response);
            return;
        }
        //如果需要处理，判断是否已经登录
        Object employee = request.getSession().getAttribute("employee");
        if (employee != null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("employee"));
            chain.doFilter(request,response);
            return;
        }
        Object user = request.getSession().getAttribute("user");
        if (user != null){
            log.info("移动端用户已登录，用户id为：{}",request.getSession().getAttribute("user"));
            chain.doFilter(request,response);
            return;
        }
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(Result.error("NOTLOGIN")));
        return;
    }
    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }
}

