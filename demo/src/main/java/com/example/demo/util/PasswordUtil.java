package com.example.demo.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 加密：把用户输入的明文密码变成密文
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    // 验证：用户登录时，把明文密码和数据库里的密文对比
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}