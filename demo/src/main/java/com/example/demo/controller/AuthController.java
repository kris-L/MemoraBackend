package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private VerificationCodeService verificationCodeService;

    // 发送注册邮箱验证码接口
    @PostMapping("/send-register-email-code")
    public Map<String, Object> sendRegisterEmailCode(@RequestParam String email) {
        Map<String, Object> result = new HashMap<>();
        // 检查邮箱是否已被注册
        if (userService.findByEmail(email) != null) {
            result.put("code", 400);
            result.put("message", "该邮箱已被注册");
            return result;
        }
        try {
            verificationCodeService.sendEmailCode(email, 1, "注册");
            result.put("code", 200);
            result.put("message", "邮箱验证码已发送");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "发送失败: " + e.getMessage());
        }
        return result;
    }

    // 发送注册手机验证码接口（预留）
    @PostMapping("/send-register-phone-code")
    public Map<String, Object> sendRegisterPhoneCode(@RequestParam String phone) {
        Map<String, Object> result = new HashMap<>();
        // 检查手机号格式
        if (!com.example.demo.util.ValidationUtil.isValidPhone(phone)) {
            result.put("code", 400);
            result.put("message", "手机号格式不正确");
            return result;
        }
        
        // 检查手机号是否已被注册
        if (userService.findByPhone(phone) != null) {
            result.put("code", 400);
            result.put("message", "该手机号已被注册");
            return result;
        }
        
        try {
            verificationCodeService.sendPhoneCode(phone, 5, "注册"); // type=5表示注册手机验证码
            result.put("code", 200);
            result.put("message", "手机验证码已发送");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "发送失败: " + e.getMessage());
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
        // 1. 先校验验证码（安全加强）
        System.out.println("[注册安全] 开始验证注册验证码 - 邮箱: " + email + ", 验证码: " + code);
        if (!verificationCodeService.verifyCode(email, 1, code)) {
            System.out.println("[注册安全] 验证码验证失败，拒绝注册");
            result.put("code", 400);
            result.put("message", "验证码错误或已过期");
            return result;
        }
        System.out.println("[注册安全] 验证码验证通过");
        // 2. 调用原有的注册逻辑
        try {
            boolean success = userService.register(nickname, phone, email, password);
            if (success) {
                result.put("code", 200);
                result.put("message", "注册成功");
            } else {
                result.put("code", 500);
                result.put("message", "注册失败");
            }
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误");
        }
        return result;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String account,
                                     @RequestParam String password) {
        Map<String, Object> result = new HashMap<>();
        String token = userService.login(account, password);
        if (token != null) {
            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("token", token);
        } else {
            result.put("code", 401);
            result.put("message", "账号或密码错误");
        }
        return result;
    }

    @DeleteMapping("/account")
    public Map<String, Object> deleteAccount(@RequestHeader("token") String token) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getUserByToken(token);
        if (user == null) {
            result.put("code", 401);
            result.put("message", "无效的 token，请先登录");
            return result;
        }
        try {
            boolean success = userService.deleteAccount(user.getId());
            if (success) {
                result.put("code", 200);
                result.put("message", "账号已注销");
            } else {
                result.put("code", 500);
                result.put("message", "注销失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误");
        }
        return result;
    }

    // 发送重置密码验证码接口
    @PostMapping("/forgot/send-code")
    public Map<String, Object> sendResetCode(@RequestParam String email) {
        Map<String, Object> result = new HashMap<>();
        // 检查邮箱是否存在
        User user = userService.findByEmail(email);
        if (user == null) {
            result.put("code", 400);
            result.put("message", "该邮箱未注册");
            return result;
        }
        try {
            verificationCodeService.sendEmailCode(email, 2, "重置密码");
            result.put("code", 200);
            result.put("message", "验证码已发送");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "发送失败");
        }
        return result;
    }

    // 重置密码接口
    @PostMapping("/forgot/reset")
    public Map<String, Object> resetPassword(@RequestParam String email,
                                             @RequestParam String code,
                                             @RequestParam String newPassword) {
        Map<String, Object> result = new HashMap<>();
        // 1. 验证验证码（安全加强）
        System.out.println("[重置密码安全] 开始验证重置密码验证码 - 邮箱: " + email + ", 验证码: " + code);
        if (!verificationCodeService.verifyCode(email, 2, code)) {
            System.out.println("[重置密码安全] 验证码验证失败，拒绝重置");
            result.put("code", 400);
            result.put("message", "验证码错误或已过期");
            return result;
        }
        System.out.println("[重置密码安全] 验证码验证通过");
        // 2. 更新密码
        try {
            userService.updatePasswordByEmail(email, newPassword);
            result.put("code", 200);
            result.put("message", "密码重置成功");
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误");
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
            result.put("code", 401);
            result.put("message", "无效的 token，请先登录");
            return result;
        }
        
        try {
            userService.updatePasswordWithOld(user.getId(), oldPassword, newPassword);
            result.put("code", 200);
            result.put("message", "密码修改成功");
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误");
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
            result.put("code", 401);
            result.put("message", "无效的 token，请先登录");
            return result;
        }
        
        try {
            userService.updateNickname(user.getId(), nickname);
            result.put("code", 200);
            result.put("message", "昵称修改成功");
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误");
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
            result.put("code", 401);
            result.put("message", "无效的 token，请先登录");
            return result;
        }
        
        // 检查手机号格式
        if (!com.example.demo.util.ValidationUtil.isValidPhone(phone)) {
            result.put("code", 400);
            result.put("message", "手机号格式不正确");
            return result;
        }
        
        // 检查手机号是否已被其他用户绑定
        User existingUser = userService.findByPhone(phone);
        if (existingUser != null && !existingUser.getId().equals(user.getId())) {
            result.put("code", 400);
            result.put("message", "该手机号已被其他用户绑定");
            return result;
        }
        
        try {
            // 发送手机验证码（现已实现）
            verificationCodeService.sendPhoneCode(phone, 3, "绑定手机");
            result.put("code", 200);
            result.put("message", "手机验证码发送成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "验证码发送失败: " + e.getMessage());
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
            result.put("code", 401);
            result.put("message", "无效的 token，请先登录");
            return result;
        }
        
        // 验证手机验证码
        if (!verificationCodeService.verifyCode(phone, 3, code)) {
            result.put("code", 400);
            result.put("message", "验证码错误或已过期");
            return result;
        }

        // 唯一性检查已经在service中处理
        try {
            userService.updatePhone(user.getId(), phone);
            result.put("code", 200);
            result.put("message", "手机绑定成功");
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误");
        }
        return result;
    }

    // 7.4 解绑手机
    @DeleteMapping("/phone")
    public Map<String, Object> unbindPhone(@RequestHeader("token") String token) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getUserByToken(token);
        if (user == null) {
            result.put("code", 401);
            result.put("message", "无效的 token，请先登录");
            return result;
        }
        
        try {
            userService.unbindPhone(user.getId());
            result.put("code", 200);
            result.put("message", "手机解绑成功");
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误");
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
            result.put("code", 401);
            result.put("message", "无效的 token，请先登录");
            return result;
        }
        
        // 检查邮箱是否已被其他用户绑定
        User existingUser = userService.findByEmail(email);
        if (existingUser != null && !existingUser.getId().equals(user.getId())) {
            result.put("code", 400);
            result.put("message", "该邮箱已被其他用户绑定");
            return result;
        }
        
        try {
            verificationCodeService.sendEmailCode(email, type, "绑定邮箱");
            result.put("code", 200);
            result.put("message", "邮箱验证码已发送");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "发送失败");
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
            result.put("code", 401);
            result.put("message", "无效的 token，请先登录");
            return result;
        }
        
        // 验证验证码
        if (!verificationCodeService.verifyCode(email, 4, code)) {
            result.put("code", 400);
            result.put("message", "验证码错误或已过期");
            return result;
        }
        
        try {
            userService.updateEmail(user.getId(), email);
            result.put("code", 200);
            result.put("message", "邮箱绑定成功");
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误");
        }
        return result;
    }

    // 7.5.3 解绑邮箱
    @DeleteMapping("/email")
    public Map<String, Object> unbindEmail(@RequestHeader("token") String token) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getUserByToken(token);
        if (user == null) {
            result.put("code", 401);
            result.put("message", "无效的 token，请先登录");
            return result;
        }
        
        try {
            userService.unbindEmail(user.getId());
            result.put("code", 200);
            result.put("message", "邮箱解绑成功");
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误");
        }
        return result;
    }
}