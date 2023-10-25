package com.cao.ruijie;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@Slf4j
@SpringBootApplication
class RuijieApplicationTests {
    public static void main(String[] args) {
        SpringApplication.run(RuijieApplicationTests.class,args);
        log.info("项目启动成功");
    }

}


