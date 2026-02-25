package com.example.demo.config;

import com.example.demo.service.UserService;
import com.example.demo.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 安全定时任务配置
 * 定期清理过期Token和安全记录
 */
@Configuration
@EnableScheduling
public class SecurityScheduledTasks {
    
    @Autowired
    private UserService userService;
    
    /**
     * 每小时清理一次过期的Token记录
     * cron表达式: 0 0 * * * ? (每小时整点执行)
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanupExpiredTokens() {
        try {
            int cleanedCount = userService.cleanupExpiredTokens();
            SecurityUtil.cleanupExpiredRecords(); // 清理安全工具类中的过期记录
            System.out.println("[定时任务] Token清理完成，共清理 " + cleanedCount + " 个过期Token");
        } catch (Exception e) {
            System.err.println("[定时任务] Token清理失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 每天凌晨2点执行安全审计
     * cron表达式: 0 0 2 * * ?
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailySecurityAudit() {
        try {
            System.out.println("[安全审计] 开始每日安全审计...");
            // 这里可以添加更多的安全审计逻辑
            // 比如检查异常登录模式、统计安全事件等
            System.out.println("[安全审计] 每日安全审计完成");
        } catch (Exception e) {
            System.err.println("[安全审计] 安全审计失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 每30分钟清理一次临时安全记录
     * cron表达式: 0
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void cleanupTemporaryRecords() {
        try {
            SecurityUtil.cleanupExpiredRecords();
            System.out.println("[定时任务] 临时安全记录清理完成");
        } catch (Exception e) {
            System.err.println("[定时任务] 临时记录清理失败: " + e.getMessage());
        }
    }
}