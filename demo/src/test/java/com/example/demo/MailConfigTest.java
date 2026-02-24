package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
public class MailConfigTest {

    @Autowired
    private JavaMailSender mailSender;

    @Test
    public void testMailConfiguration() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("2129192040@qq.com");
            message.setTo("2129192040@qq.com"); // 发送给自己测试
            message.setSubject("测试邮件");
            message.setText("这是一封测试邮件，用于验证邮件配置是否正确。");
            
            mailSender.send(message);
            System.out.println("邮件发送成功！");
        } catch (Exception e) {
            System.err.println("邮件发送失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}