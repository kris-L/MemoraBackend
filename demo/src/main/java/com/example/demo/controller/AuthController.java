package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.service.VerificationCodeService;
import com.example.demo.util.SecurityUtil;
import com.example.demo.util.ValidationUtil;
import com.example.demo.util.SecurityLogger;
import com.example.demo.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private VerificationCodeService verificationCodeService;
    
    @Autowired
    private SecurityLogger securityLogger;

    // 发送注册邮箱验证码接口
    @PostMapping("/send-register-email-code")
    public Map<String, Object> sendRegisterEmailCode(@RequestParam String email) {
        Map<String, Object> result = new HashMap<>();
        
        // 输入验证
        if (!ValidationUtil.isValidEmail(email)) {
            throw new BusinessException(400, "邮箱格式不正确");
        }
        
        // 频率限制检查
        if (!SecurityUtil.canSendCode(email)) {
            throw new BusinessException(429, "发送过于频繁，请稍后再试");
        }
        
        // 检查邮箱是否已被注册
        if (userService.findByEmail(email) != null) {
            throw new BusinessException(400, "该邮箱已被注册");
        }
        
        try {
            verificationCodeService.sendEmailCode(email, 1, "注册");
            SecurityUtil.recordCodeSendTime(email); // 记录发送时间
            result.put("code", 200);
            result.put("message", "邮箱验证码已发送");
        } catch (Exception e) {
            throw new BusinessException(500, "验证码发送失败");
        }
        return result;
    }

    // 发送注册手机验证码接口（预留）
    @PostMapping("/send-register-phone-code")
    public Map<String, Object> sendRegisterPhoneCode(@RequestParam String phone) {
        Map<String, Object> result = new HashMap<>();
        
        // 输入验证
        if (!ValidationUtil.isValidPhone(phone)) {
            throw new BusinessException(400, "手机号格式不正确");
        }
        
        // 频率限制检查
        if (!SecurityUtil.canSendCode(phone)) {
            throw new BusinessException(429, "发送过于频繁，请稍后再试");
        }
        
        // 检查手机号是否已被注册
        if (userService.findByPhone(phone) != null) {
            throw new BusinessException(400, "该手机号已被注册");
        }
        
        try {
            verificationCodeService.sendPhoneCode(phone, 5, "注册"); // type=5表示注册手机验证码
            SecurityUtil.recordCodeSendTime(phone); // 记录发送时间
            result.put("code", 200);
            result.put("message", "手机验证码已发送");
        } catch (Exception e) {
            throw new BusinessException(500, "验证码发送失败");
        }
        return result;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestParam String nickname,
                                        @RequestParam(required = false) String phone,
                                        @RequestParam String email,
                                        @RequestParam String password,
                                        @RequestParam String code) {
        Map<String, Object> result = new HashMap<>();
        
        // 输入验证
        if (!ValidationUtil.isValidNickname(nickname)) {
            throw new BusinessException(400, "昵称格式不正确");
        }
        if (phone != null && !ValidationUtil.isValidPhone(phone)) {
            throw new BusinessException(400, "手机号格式不正确");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new BusinessException(400, "邮箱格式不正确");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            throw new BusinessException(400, "密码不符合要求（至少8位，包含字母和数字）");
        }
        if (!ValidationUtil.isValidCode(code)) {
            throw new BusinessException(400, "验证码格式不正确");
        }
        
        // 1. 先校验验证码（安全加强）
        System.out.println("[注册安全] 开始验证注册验证码 - 邮箱: " + email + ", 验证码: " + code);
        if (!verificationCodeService.verifyCode(email, 1, code)) {
            System.out.println("[注册安全] 验证码验证失败，拒绝注册");
            throw new BusinessException(400, "验证码错误或已过期");
        }
        System.out.println("[注册安全] 验证码验证通过");
        
        // 2. 调用原有的注册逻辑
        try {
            boolean success = userService.register(nickname, phone, email, password);
            if (success) {
                result.put("code", 200);
                result.put("message", "注册成功");
            } else {
                throw new BusinessException(500, "注册失败");
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
        return result;
    }

    @PostMapping("/login")
    public Map<String, Object> login(HttpServletRequest request, 
                                     @RequestParam String account,
                                     @RequestParam String password) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取客户端信息
        String clientIp = securityLogger.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        // 输入验证
        if (account == null || account.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            securityLogger.logLoginFailure(account, clientIp, userAgent, "账号或密码为空");
            throw new BusinessException(400, "账号和密码不能为空");
        }
        
        // 账户锁定检查
        if (SecurityUtil.isAccountLocked(account)) {
            long remainingTime = SecurityUtil.getRemainingLockoutTime(account);
            securityLogger.logAccountLocked(account, clientIp);
            throw new BusinessException(423, "账户已被锁定，请" + remainingTime + "秒后再试");
        }
        
        String token = userService.login(account, password);
        if (token != null) {
            // 登录成功，重置失败次数
            SecurityUtil.resetLoginAttempts(account);
            securityLogger.logLoginSuccess(account, clientIp, userAgent);
            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("token", token);
        } else {
            // 登录失败，记录失败次数
            SecurityUtil.recordLoginFailure(account);
            securityLogger.logLoginFailure(account, clientIp, userAgent, "账号或密码错误");
            
            // 检查是否需要锁定账户
            if (SecurityUtil.shouldLockAccount(account)) {
                long remainingTime = SecurityUtil.getRemainingLockoutTime(account);
                securityLogger.logAccountLocked(account, clientIp);
                throw new BusinessException(423, "登录失败次数过多，账户已被锁定" + remainingTime + "秒");
            }
            
            throw new BusinessException(401, "账号或密码错误");
        }
        return result;
    }

    // 用户退出登录接口
    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request, 
                                      @RequestHeader("token") String token) {
        Map<String, Object> result = new HashMap<>();
        
        User user = userService.getUserByToken(token);
        if (user == null) {
            result.put("code", 200); // 即使token无效也返回成功
            result.put("message", "退出成功");
            return result;
        }
        
        // 清除用户Token
        userService.clearUserToken(user.getId());
        
        // 记录安全日志
        String clientIp = securityLogger.getClientIp(request);
        securityLogger.logSensitiveOperation(String.valueOf(user.getId()), "用户退出", clientIp, "正常退出登录");
        
        result.put("code", 200);
        result.put("message", "退出成功");
        return result;
    }
    
    // Token刷新接口
    @PostMapping("/refresh-token")
    public Map<String, Object> refreshToken(HttpServletRequest request,
                                           @RequestHeader("token") String oldToken) {
        Map<String, Object> result = new HashMap<>();
        
        User user = userService.getUserByToken(oldToken);
        if (user == null) {
            throw new BusinessException(401, "无效的 token，请先登录");
        }
        
        // 刷新Token
        String newToken = userService.refreshToken(oldToken);
        
        if (newToken != null && !newToken.equals(oldToken)) {
            // 记录Token刷新日志
            String clientIp = securityLogger.getClientIp(request);
            securityLogger.logTokenRefresh(String.valueOf(user.getId()), oldToken, newToken, clientIp);
            
            result.put("code", 200);
            result.put("message", "Token刷新成功");
            result.put("token", newToken);
        } else {
            // Token未临近过期，返回原Token
            result.put("code", 200);
            result.put("message", "Token有效，无需刷新");
            result.put("token", oldToken);
        }
        
        return result;
    }
    
    @DeleteMapping("/account")
    public Map<String, Object> deleteAccount(@RequestHeader("token") String token) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getUserByToken(token);
        if (user == null) {
            throw new BusinessException(401, "无效的 token，请先登录");
        }
        try {
            boolean success = userService.deleteAccount(user.getId());
            if (success) {
                result.put("code", 200);
                result.put("message", "账号已注销");
            } else {
                throw new BusinessException(500, "注销失败");
            }
        } catch (Exception e) {
            throw new BusinessException(500, "服务器内部错误");
        }
        return result;
    }

    // 发送重置密码验证码接口
    @PostMapping("/forgot/send-code")
    public Map<String, Object> sendResetCode(@RequestParam String email) {
        Map<String, Object> result = new HashMap<>();
        
        // 输入验证
        if (!ValidationUtil.isValidEmail(email)) {
            throw new BusinessException(400, "邮箱格式不正确");
        }
        
        // 频率限制检查
        if (!SecurityUtil.canSendCode(email)) {
            throw new BusinessException(429, "发送过于频繁，请稍后再试");
        }
        
        // 检查邮箱是否存在
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new BusinessException(400, "该邮箱未注册");
        }
        
        try {
            verificationCodeService.sendEmailCode(email, 2, "重置密码");
            SecurityUtil.recordCodeSendTime(email); // 记录发送时间
            result.put("code", 200);
            result.put("message", "验证码已发送");
        } catch (Exception e) {
            throw new BusinessException(500, "验证码发送失败");
        }
        return result;
    }

    // 重置密码接口
    @PostMapping("/forgot/reset")
    public Map<String, Object> resetPassword(@RequestParam String email,
                                             @RequestParam String code,
                                             @RequestParam String newPassword) {
        Map<String, Object> result = new HashMap<>();
        
        // 输入验证
        if (!ValidationUtil.isValidEmail(email)) {
            throw new BusinessException(400, "邮箱格式不正确");
        }
        if (!ValidationUtil.isValidCode(code)) {
            throw new BusinessException(400, "验证码格式不正确");
        }
        if (!ValidationUtil.isValidPassword(newPassword)) {
            throw new BusinessException(400, "密码不符合要求（至少8位，包含字母和数字）");
        }
        
        // 1. 验证验证码（安全加强）
        System.out.println("[重置密码安全] 开始验证重置密码验证码 - 邮箱: " + email + ", 验证码: " + code);
        if (!verificationCodeService.verifyCode(email, 2, code)) {
            System.out.println("[重置密码安全] 验证码验证失败，拒绝重置");
            throw new BusinessException(400, "验证码错误或已过期");
        }
        System.out.println("[重置密码安全] 验证码验证通过");
        
        // 2. 更新密码
        try {
            userService.updatePasswordByEmail(email, newPassword);
            result.put("code", 200);
            result.put("message", "密码重置成功");
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
        return result;
    }

    // 7.1 修改密码接口（需旧密码验证）
    @PutMapping("/password")
    public Map<String, Object> updatePassword(@RequestHeader("token") String token,
                                              @RequestParam String oldPassword,
                                              @RequestParam String newPassword) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getUserByToken(token);
        if (user == null) {
            throw new BusinessException(401, "无效的 token，请先登录");
        }
        
        // 输入验证
        if (!ValidationUtil.isValidPassword(newPassword)) {
            throw new BusinessException(400, "新密码不符合要求");
        }
        
        try {
            userService.updatePasswordWithOld(user.getId(), oldPassword, newPassword);
            result.put("code", 200);
            result.put("message", "密码修改成功");
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
        return result;
    }

    // 7.2 修改昵称接口
    @PutMapping("/nickname")
    public Map<String, Object> updateNickname(@RequestHeader("token") String token,
                                              @RequestParam String nickname) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getUserByToken(token);
        if (user == null) {
            throw new BusinessException(401, "无效的 token，请先登录");
        }
        
        // 输入验证
        if (!ValidationUtil.isValidNickname(nickname)) {
            throw new BusinessException(400, "昵称格式不正确");
        }
        
        try {
            userService.updateNickname(user.getId(), nickname);
            result.put("code", 200);
            result.put("message", "昵称修改成功");
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
        return result;
    }

    // 7.3.1 发送绑定手机验证码
    @PostMapping("/send-phone-code")
    public Map<String, Object> sendPhoneCode(@RequestHeader("token") String token,
                                             @RequestParam String phone) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getUserByToken(token);
        if (user == null) {
            throw new BusinessException(401, "无效的 token，请先登录");
        }
        
        // 输入验证
        if (!ValidationUtil.isValidPhone(phone)) {
            throw new BusinessException(400, "手机号格式不正确");
        }
        
        // 频率限制检查
        if (!SecurityUtil.canSendCode(phone)) {
            throw new BusinessException(429, "发送过于频繁，请稍后再试");
        }
        
        // 检查手机号是否已被其他用户绑定
        User existingUser = userService.findByPhone(phone);
        if (existingUser != null && !existingUser.getId().equals(user.getId())) {
            throw new BusinessException(400, "该手机号已被其他用户绑定");
        }
        
        try {
            // 发送手机验证码（现已实现）
            verificationCodeService.sendPhoneCode(phone, 3, "绑定手机");
            SecurityUtil.recordCodeSendTime(phone); // 记录发送时间
            result.put("code", 200);
            result.put("message", "手机验证码发送成功");
        } catch (Exception e) {
            throw new BusinessException(500, "验证码发送失败");
        }
        return result;
    }

    // 7.3.2 绑定手机接口
    @PutMapping("/phone")
    public Map<String, Object> bindPhone(@RequestHeader("token") String token,
                                         @RequestParam String phone,
                                         @RequestParam String code) {   // 验证码（必填）
        Map<String, Object> result = new HashMap<>();
        User user = userService.getUserByToken(token);
        if (user == null) {
            throw new BusinessException(401, "无效的 token，请先登录");
        }
        
        // 输入验证
        if (!ValidationUtil.isValidPhone(phone)) {
            throw new BusinessException(400, "手机号格式不正确");
        }
        if (!ValidationUtil.isValidCode(code)) {
            throw new BusinessException(400, "验证码格式不正确");
        }
        
        // 验证手机验证码
        if (!verificationCodeService.verifyCode(phone, 3, code)) {
            throw new BusinessException(400, "验证码错误或已过期");
        }

        // 唯一性检查已经在service中处理
        try {
            userService.updatePhone(user.getId(), phone);
            result.put("code", 200);
            result.put("message", "手机绑定成功");
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
        return result;
    }

    // 7.4 解绑手机
    @DeleteMapping("/phone")
    public Map<String, Object> unbindPhone(@RequestHeader("token") String token) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getUserByToken(token);
        if (user == null) {
            throw new BusinessException(401, "无效的 token，请先登录");
        }
        
        try {
            userService.unbindPhone(user.getId());
            result.put("code", 200);
            result.put("message", "手机解绑成功");
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
        return result;
    }

    // 7.5.1 发送绑定邮箱验证码
    @PostMapping("/send-email-code")
    public Map<String, Object> sendBindEmailCode(@RequestHeader("token") String token,
                                                 @RequestParam String email,
                                                 @RequestParam Integer type) { // type=4 绑定邮箱
        Map<String, Object> result = new HashMap<>();
        User user = userService.getUserByToken(token);
        if (user == null) {
            throw new BusinessException(401, "无效的 token，请先登录");
        }
        
        // 输入验证
        if (!ValidationUtil.isValidEmail(email)) {
            throw new BusinessException(400, "邮箱格式不正确");
        }
        
        // 频率限制检查
        if (!SecurityUtil.canSendCode(email)) {
            throw new BusinessException(429, "发送过于频繁，请稍后再试");
        }
        
        // 检查邮箱是否已被其他用户绑定
        User existingUser = userService.findByEmail(email);
        if (existingUser != null && !existingUser.getId().equals(user.getId())) {
            throw new BusinessException(400, "该邮箱已被其他用户绑定");
        }
        
        try {
            verificationCodeService.sendEmailCode(email, type, "绑定邮箱");
            SecurityUtil.recordCodeSendTime(email); // 记录发送时间
            result.put("code", 200);
            result.put("message", "邮箱验证码已发送");
        } catch (Exception e) {
            throw new BusinessException(500, "验证码发送失败");
        }
        return result;
    }

    // 7.5.2 绑定邮箱接口
    @PutMapping("/email")
    public Map<String, Object> bindEmail(@RequestHeader("token") String token,
                                         @RequestParam String email,
                                         @RequestParam String code) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getUserByToken(token);
        if (user == null) {
            throw new BusinessException(401, "无效的 token，请先登录");
        }
        
        // 输入验证
        if (!ValidationUtil.isValidEmail(email)) {
            throw new BusinessException(400, "邮箱格式不正确");
        }
        if (!ValidationUtil.isValidCode(code)) {
            throw new BusinessException(400, "验证码格式不正确");
        }
        
        // 验证验证码
        if (!verificationCodeService.verifyCode(email, 4, code)) {
            throw new BusinessException(400, "验证码错误或已过期");
        }
        
        try {
            userService.updateEmail(user.getId(), email);
            result.put("code", 200);
            result.put("message", "邮箱绑定成功");
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
        return result;
    }

    // 7.5.3 解绑邮箱
    @DeleteMapping("/email")
    public Map<String, Object> unbindEmail(@RequestHeader("token") String token) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getUserByToken(token);
        if (user == null) {
            throw new BusinessException(401, "无效的 token，请先登录");
        }
        
        try {
            userService.unbindEmail(user.getId());
            result.put("code", 200);
            result.put("message", "邮箱解绑成功");
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
        return result;
    }
}