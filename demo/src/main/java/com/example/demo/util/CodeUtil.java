package com.example.demo.util;

import java.util.Random;

public class CodeUtil {
    public static String generate6DigitCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 100000~999999
        return String.valueOf(code);
    }
}