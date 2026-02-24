package com.example.demo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

@Component
public class DatabaseCheckUtil implements ApplicationRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            // 检查用户表是否存在
            Integer userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'appmemory_db' AND table_name = 'user'", 
                Integer.class);
            
            if (userCount == 0) {
                System.out.println("警告: user表不存在，请先创建数据库表");
            } else {
                System.out.println("user表存在");
            }
            
            // 检查verification_code表是否存在
            Integer codeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'appmemory_db' AND table_name = 'verification_code'", 
                Integer.class);
            
            if (codeCount == 0) {
                System.out.println("警告: verification_code表不存在，正在创建...");
                createVerificationCodeTable();
            } else {
                System.out.println("verification_code表存在");
            }
            
        } catch (Exception e) {
            System.err.println("数据库连接检查失败: " + e.getMessage());
            System.err.println("请确认:");
            System.err.println("1. MySQL服务是否启动");
            System.err.println("2. 数据库appmemory_db是否存在");
            System.err.println("3. 用户名密码是否正确");
        }
    }

    private void createVerificationCodeTable() {
        String createTableSql = """
            CREATE TABLE IF NOT EXISTS verification_code (
                id INT AUTO_INCREMENT PRIMARY KEY,
                target VARCHAR(100) NOT NULL,
                code VARCHAR(10) NOT NULL,
                type TINYINT NOT NULL,
                expiry_time DATETIME NOT NULL,
                used TINYINT DEFAULT 0,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_target_type (target, type),
                INDEX idx_expiry_time (expiry_time)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        
        try {
            jdbcTemplate.execute(createTableSql);
            System.out.println("verification_code表创建成功");
            // 插入一条测试数据验证表是否正常工作
            String testInsertSql = "INSERT INTO verification_code (target, code, type, expiry_time, used) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(testInsertSql, "test@example.com", "123456", 1, new java.util.Date(System.currentTimeMillis() + 600000), 0);
            System.out.println("测试数据插入成功");
        } catch (Exception e) {
            System.err.println("创建verification_code表失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}