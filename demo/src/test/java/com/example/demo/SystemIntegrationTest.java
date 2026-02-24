package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SystemIntegrationTest {

    @Test
    public void contextLoads() {
        // 测试Spring上下文是否能正常加载
        System.out.println("Spring Boot应用上下文加载成功");
    }
}