package com.example.demo;

import com.example.demo.util.CodeUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ForgotPasswordTest {

    @Test
    public void testCodeGeneration() {
        String code = CodeUtil.generate6DigitCode();
        System.out.println("忘记密码验证码: " + code);
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));
    }
}