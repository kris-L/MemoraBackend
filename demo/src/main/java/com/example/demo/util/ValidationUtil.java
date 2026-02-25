package com.example.demo.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    // 手机号正则（更严格的验证）
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    
    // 邮箱正则（更严格的验证）
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    // 密码复杂度正则（至少8位，包含字母和数字）
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$";
    
    // 昵称正则（只允许中文、英文字母、数字、下划线，长度1-20）
    private static final String NICKNAME_REGEX = "^[\\u4e00-\\u9fa5A-Za-z0-9_]{1,20}$";
    
    // 验证码正则（6位数字）
    private static final String CODE_REGEX = "^\\d{6}$";

    // 验证手机号
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        if (phone.length() != 11) return false;
        return Pattern.matches(PHONE_REGEX, phone);
    }

    // 验证邮箱
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        if (email.length() > 100) return false; // 限制邮箱长度
        return Pattern.matches(EMAIL_REGEX, email);
    }

    // 验证密码（加强版）
    public static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) return false;
        if (password.length() < 8 || password.length() > 50) return false;
        return Pattern.matches(PASSWORD_REGEX, password);
    }

    // 验证昵称（加强版）
    public static boolean isValidNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) return false;
        if (nickname.length() > 20) return false;
        return Pattern.matches(NICKNAME_REGEX, nickname);
    }
    
    // 验证验证码
    public static boolean isValidCode(String code) {
        if (code == null || code.trim().isEmpty()) return false;
        return Pattern.matches(CODE_REGEX, code);
    }
    
    // 验证字符串长度范围
    public static boolean isValidLength(String str, int minLength, int maxLength) {
        if (str == null) return false;
        int len = str.length();
        return len >= minLength && len <= maxLength;
    }
    
    // 验证是否包含非法字符
    public static boolean containsIllegalChars(String str) {
        if (str == null) return false;
        // 检查常见的SQL注入字符
        String[] illegalChars = {"'", "\"", "--", ";", "/*", "*/", "<script", "</script>"};
        for (String illegalChar : illegalChars) {
            if (str.toLowerCase().contains(illegalChar.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    // 安全的字符串清理（移除危险字符）
    public static String sanitizeInput(String input) {
        if (input == null) return null;
        return input.replaceAll("[<>\"']", "")
                   .replaceAll("--", "")
                   .replaceAll(";", "");
    }
}