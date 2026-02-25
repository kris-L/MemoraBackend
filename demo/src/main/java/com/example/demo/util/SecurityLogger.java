package com.example.demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 安全日志记录工具类
 * 记录敏感操作和安全事件
 */
@Component
public class SecurityLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityLogger.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 记录登录成功事件
     */
    public void logLoginSuccess(String account, String ip, String userAgent) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        logger.info("[安全日志] {} - 用户登录成功 - 账号: {}, IP: {}, User-Agent: {}", 
                   timestamp, maskAccount(account), ip, userAgent);
    }
    
    /**
     * 记录登录失败事件
     */
    public void logLoginFailure(String account, String ip, String userAgent, String reason) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        logger.warn("[安全警告] {} - 用户登录失败 - 账号: {}, IP: {}, User-Agent: {}, 原因: {}", 
                   timestamp, maskAccount(account), ip, userAgent, reason);
    }
    
    /**
     * 记录账户锁定事件
     */
    public void logAccountLocked(String account, String ip) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        logger.error("[安全警报] {} - 账户被锁定 - 账号: {}, IP: {}", 
                    timestamp, maskAccount(account), ip);
    }
    
    /**
     * 记录Token刷新事件
     */
    public void logTokenRefresh(String userId, String oldToken, String newToken, String ip) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        logger.info("[安全日志] {} - Token刷新 - 用户ID: {}, 旧Token: {}, 新Token: {}, IP: {}", 
                   timestamp, userId, maskToken(oldToken), maskToken(newToken), ip);
    }
    
    /**
     * 记录Token过期清理事件
     */
    public void logTokenCleanup(String userId, String token, String reason) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        logger.info("[安全日志] {} - Token清理 - 用户ID: {}, Token: {}, 原因: {}", 
                   timestamp, userId, maskToken(token), reason);
    }
    
    /**
     * 记录密码修改事件
     */
    public void logPasswordChange(String userId, String ip, boolean isReset) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String operation = isReset ? "重置密码" : "修改密码";
        logger.info("[安全日志] {} - {} - 用户ID: {}, IP: {}", 
                   timestamp, operation, userId, ip);
    }
    
    /**
     * 记录敏感操作事件
     */
    public void logSensitiveOperation(String userId, String operation, String ip, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        logger.info("[安全日志] {} - 敏感操作 - 用户ID: {}, 操作: {}, IP: {}, 详情: {}", 
                   timestamp, userId, operation, ip, details);
    }
    
    /**
     * 记录安全异常事件
     */
    public void logSecurityException(String eventType, String ip, String userAgent, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        logger.error("[安全异常] {} - {} - IP: {}, User-Agent: {}, 详情: {}", 
                    timestamp, eventType, ip, userAgent, details);
    }
    
    /**
     * 从HTTP请求中提取客户端IP
     */
    public String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * 掩码账号信息（保护隐私）
     */
    private String maskAccount(String account) {
        if (account == null || account.isEmpty()) {
            return "未知账号";
        }
        
        if (account.contains("@")) {
            // 邮箱掩码
            String[] parts = account.split("@");
            if (parts.length == 2) {
                String username = parts[0];
                String domain = parts[1];
                if (username.length() > 2) {
                    return username.substring(0, 2) + "***@" + domain;
                }
            }
        } else if (account.matches("^1[3-9]\\d{9}$")) {
            // 手机号掩码
            return account.substring(0, 3) + "****" + account.substring(7);
        }
        
        // 其他情况返回部分掩码
        if (account.length() > 3) {
            return account.substring(0, 3) + "***";
        }
        return "***";
    }
    
    /**
     * 掩码Token信息
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "无效Token";
        }
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }
}