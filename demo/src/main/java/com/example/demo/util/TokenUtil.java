package com.example.demo.util;

import java.util.Date;
import java.util.UUID;

public class TokenUtil {
    
    // Token默认有效期（30天）
    private static final long DEFAULT_EXPIRY_MILLIS = 30L * 24 * 60 * 60 * 1000;
    
    // Token刷新阈值（7天内过期则可刷新）
    private static final long REFRESH_THRESHOLD_MILLIS = 7L * 24 * 60 * 60 * 1000;
    
    public static String generateToken() {
        // 生成一个不带横线的随机字符串
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 检查Token是否即将过期（可用于刷新判断）
     */
    public static boolean isTokenExpiringSoon(Date expiryTime) {
        if (expiryTime == null) {
            return true;
        }
        long timeToExpiry = expiryTime.getTime() - System.currentTimeMillis();
        return timeToExpiry <= REFRESH_THRESHOLD_MILLIS && timeToExpiry > 0;
    }
    
    /**
     * 检查Token是否已过期
     */
    public static boolean isTokenExpired(Date expiryTime) {
        if (expiryTime == null) {
            return true;
        }
        return expiryTime.before(new Date());
    }
    
    /**
     * 获取Token剩余有效时间（毫秒）
     */
    public static long getTokenRemainingTime(Date expiryTime) {
        if (expiryTime == null) {
            return 0;
        }
        long remaining = expiryTime.getTime() - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
    
    /**
     * 延长Token有效期
     */
    public static Date extendTokenExpiry(Date currentExpiry, int daysToAdd) {
        if (currentExpiry == null) {
            currentExpiry = new Date();
        }
        long newExpiryMillis = currentExpiry.getTime() + (long) daysToAdd * 24 * 60 * 60 * 1000;
        return new Date(newExpiryMillis);
    }
    
    /**
     * 生成短期Token（用于敏感操作验证）
     */
    public static String generateShortLivedToken() {
        return "temp_" + generateToken();
    }
    
    /**
     * 验证是否为有效的Token格式
     */
    public static boolean isValidTokenFormat(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        // 检查是否为UUID格式（32位十六进制字符）
        return token.matches("^[a-fA-F0-9]{32}$") || 
               token.startsWith("temp_") && token.length() > 5;
    }
}