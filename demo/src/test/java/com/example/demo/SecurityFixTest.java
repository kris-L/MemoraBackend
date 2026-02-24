package com.example.demo;

import com.example.demo.service.VerificationCodeService;
import com.example.demo.util.CodeUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SecurityFixTest {

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Test
    public void testSecurityEnhancedVerification() {
        String testEmail = "securitytest@example.com";
        String correctCode = "123456";
        String wrongCode = "654321";
        
        System.out.println("=== 安全验证测试开始 ===");
        
        // 测试1: 空验证码应该失败
        assertFalse(verificationCodeService.verifyCode(testEmail, 1, null), 
                   "空验证码应该验证失败");
        System.out.println("✓ 空验证码测试通过");
        
        // 测试2: 错误验证码应该失败
        assertFalse(verificationCodeService.verifyCode(testEmail, 1, wrongCode), 
                   "错误验证码应该验证失败");
        System.out.println("✓ 错误验证码测试通过");
        
        // 测试3: 正确格式检查
        String generatedCode = CodeUtil.generate6DigitCode();
        assertEquals(6, generatedCode.length(), "生成的验证码应该是6位");
        assertTrue(generatedCode.matches("\\d{6}"), "验证码应该只包含数字");
        System.out.println("✓ 验证码格式测试通过");
        
        System.out.println("=== 安全验证测试结束 ===");
    }
}