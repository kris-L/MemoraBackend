package com.example.demo.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    // 手机号正则（简单版：1开头的11位数字）
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";

    // 邮箱正则（简单版）
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    // 验证手机号
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return Pattern.matches(PHONE_REGEX, phone);
    }

    // 验证邮箱
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return Pattern.matches(EMAIL_REGEX, email);
    }

    // 验证密码长度至少8位
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    // 验证昵称：不能为空，长度1-50（可根据需要调整）
    public static boolean isValidNickname(String nickname) {
        return nickname != null && nickname.length() >= 1 && nickname.length() <= 50;
    }
}