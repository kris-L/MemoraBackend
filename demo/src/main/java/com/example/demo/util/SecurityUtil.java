package com.example.demo.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Date;
import java.util.Map;

/**
 * 安全工具类 - 处理登录失败次数统计和频率限制
 */
public class SecurityUtil {
    
    // 登录失败次数统计（内存缓存，生产环境建议使用Redis）
    private static final Map<String, AtomicInteger> LOGIN_ATTEMPTS = new ConcurrentHashMap<>();
    private static final Map<String, Date> LOCKOUT_UNTIL = new ConcurrentHashMap<>();
    
    // 验证码发送频率限制
    private static final Map<String, Date> CODE_SEND_TIMES = new ConcurrentHashMap<>();
    
    // 配置常量
    private static final int MAX_LOGIN_ATTEMPTS = 5;  // 最大登录失败次数
    private static final long LOCKOUT_DURATION = 30 * 60 * 1000; // 锁定30分钟
    private static final long CODE_SEND_INTERVAL = 60 * 1000; // 验证码发送间隔1分钟
    
    /**
     * 记录登录失败
     */
    public static void recordLoginFailure(String account) {
        LOGIN_ATTEMPTS.computeIfAbsent(account, k -> new AtomicInteger(0)).incrementAndGet();
    }
    
    /**
     * 重置登录失败次数
     */
    public static void resetLoginAttempts(String account) {
        LOGIN_ATTEMPTS.remove(account);
        LOCKOUT_UNTIL.remove(account);
    }
    
    /**
     * 检查账户是否被锁定
     */
    public static boolean isAccountLocked(String account) {
        Date lockoutTime = LOCKOUT_UNTIL.get(account);
        if (lockoutTime == null) {
            return false;
        }
        
        if (new Date().after(lockoutTime)) {
            // 锁定期已过，解锁账户
            LOCKOUT_UNTIL.remove(account);
            LOGIN_ATTEMPTS.remove(account);
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查是否需要锁定账户
     */
    public static boolean shouldLockAccount(String account) {
        AtomicInteger attempts = LOGIN_ATTEMPTS.get(account);
        if (attempts != null && attempts.get() >= MAX_LOGIN_ATTEMPTS) {
            LOCKOUT_UNTIL.put(account, new Date(System.currentTimeMillis() + LOCKOUT_DURATION));
            return true;
        }
        return false;
    }
    
    /**
     * 获取剩余锁定时间（秒）
     */
    public static long getRemainingLockoutTime(String account) {
        Date lockoutTime = LOCKOUT_UNTIL.get(account);
        if (lockoutTime == null) {
            return 0;
        }
        long diff = lockoutTime.getTime() - System.currentTimeMillis();
        return Math.max(0, diff / 1000);
    }
    
    /**
     * 检查验证码发送频率限制
     */
    public static boolean canSendCode(String target) {
        Date lastSendTime = CODE_SEND_TIMES.get(target);
        if (lastSendTime == null) {
            return true;
        }
        
        long timeDiff = System.currentTimeMillis() - lastSendTime.getTime();
        return timeDiff >= CODE_SEND_INTERVAL;
    }
    
    /**
     * 记录验证码发送时间
     */
    public static void recordCodeSendTime(String target) {
        CODE_SEND_TIMES.put(target, new Date());
    }
    
    /**
     * 清理过期的记录（定时任务调用）
     */
    public static void cleanupExpiredRecords() {
        Date now = new Date();
        
        // 清理过期的锁定记录
        LOCKOUT_UNTIL.entrySet().removeIf(entry -> entry.getValue().before(now));
        
        // 清理过期的验证码发送记录（超过1小时的）
        long oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000);
        CODE_SEND_TIMES.entrySet().removeIf(entry -> entry.getValue().getTime() < oneHourAgo);
    }
}