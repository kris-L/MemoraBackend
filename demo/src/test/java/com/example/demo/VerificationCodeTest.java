package com.example.demo;

import com.example.demo.util.CodeUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VerificationCodeTest {

    @Test
    public void testGenerate6DigitCode() {
        String code = CodeUtil.generate6DigitCode();
        System.out.println("生成的验证码: " + code);
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));
        int codeNum = Integer.parseInt(code);
        assertTrue(codeNum >= 100000 && codeNum <= 999999);
    }
}