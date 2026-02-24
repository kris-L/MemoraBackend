package com.example.demo;

import com.example.demo.util.ValidationUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserInfoManagementTest {

    @Test
    public void testNicknameValidation() {
        // 测试昵称验证
        assertTrue(ValidationUtil.isValidNickname("张三"));
        assertTrue(ValidationUtil.isValidNickname("user123"));
        assertTrue(ValidationUtil.isValidNickname("测试用户昵称"));
        assertFalse(ValidationUtil.isValidNickname("")); // 空字符串
        assertFalse(ValidationUtil.isValidNickname("a".repeat(51))); // 超过50个字符
    }

    @Test
    public void testPhoneValidation() {
        // 测试手机号验证
        assertTrue(ValidationUtil.isValidPhone("13812345678"));
        assertTrue(ValidationUtil.isValidPhone("15912345678"));
        assertFalse(ValidationUtil.isValidPhone("12345")); // 太短
        assertFalse(ValidationUtil.isValidPhone("abcd1234567")); // 包含非数字
    }

    @Test
    public void testEmailValidation() {
        // 测试邮箱验证
        assertTrue(ValidationUtil.isValidEmail("test@example.com"));
        assertTrue(ValidationUtil.isValidEmail("user.name@domain.co.uk"));
        assertFalse(ValidationUtil.isValidEmail("invalid-email"));
        assertFalse(ValidationUtil.isValidEmail("@example.com"));
    }
}