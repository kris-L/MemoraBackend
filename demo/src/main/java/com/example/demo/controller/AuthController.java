package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestParam String nickname,
                                        @RequestParam(required = false) String phone,
                                        @RequestParam(required = false) String email,
                                        @RequestParam String password) {
        Map<String, Object> result = new HashMap<>();
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
}