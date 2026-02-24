package com.example.demo;

import com.example.demo.service.VerificationCodeService;
import com.example.demo.util.CodeUtil;
import com.example.demo.util.SmsUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PhoneVerificationTest {

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Test
    public void testPhoneVerificationCodeGeneration() {
        String code = CodeUtil.generate6DigitCode();
        System.out.println("生成的手机验证码: " + code);
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));
        int codeNum = Integer.parseInt(code);
        assertTrue(codeNum >= 100000 && codeNum <= 999999);
    }

    @Test
    public void testPhoneFormatValidation() {
        // 测试手机号格式判断
        assertTrue(verificationCodeService.isPhoneNumber("13812345678"));
        assertTrue(verificationCodeService.isPhoneNumber("15912345678"));
        assertFalse(verificationCodeService.isPhoneNumber("12345")); // 太短
        assertFalse(verificationCodeService.isPhoneNumber("abcd1234567")); // 包含非数字
        assertFalse(verificationCodeService.isPhoneNumber("138123456789")); // 太长
    }

    @Test
    public void testEmailFormatValidation() {
        // 测试邮箱格式判断
        assertTrue(verificationCodeService.isEmail("test@example.com"));
        assertTrue(verificationCodeService.isEmail("user.name@domain.co.uk"));
        assertFalse(verificationCodeService.isEmail("invalid-email"));
        assertFalse(verificationCodeService.isEmail("@example.com"));
        assertFalse(verificationCodeService.isEmail("test@"));
    }

    @Test
    public void testUnifiedSendCode() {
        // 测试统一的验证码发送方法
        assertDoesNotThrow(() -> {
            verificationCodeService.sendCode("13812345678", 3, "测试手机绑定");
        });
        
        assertDoesNotThrow(() -> {
            verificationCodeService.sendCode("test@example.com", 1, "测试邮箱注册");
        });
        
        // 测试不支持的格式
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            verificationCodeService.sendCode("invalid-format", 1, "测试");
        });
        assertTrue(exception.getMessage().contains("不支持的目标类型"));
    }
}