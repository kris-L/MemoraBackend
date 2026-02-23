package com.example.demo.util;

import java.util.UUID;

public class TokenUtil {
    public static String generateToken() {
        // 生成一个不带横线的随机字符串
        return UUID.randomUUID().toString().replace("-", "");
    }
}