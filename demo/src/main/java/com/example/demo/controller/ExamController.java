package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.ExamService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam")
public class ExamController {

    @Autowired
    private ExamService examService;
    @Autowired
    private UserService userService;

    // 上传考试分数接口（需要登录，所以客户端要传 token）
    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestHeader("token") String token,
                                      @RequestParam Integer score,
                                      @RequestParam String examTime) {
        Map<String, Object> result = new HashMap<>();
        // 根据 token 获取当前登录的用户
        User user = userService.getUserByToken(token);
        if (user == null) {
            result.put("code", 401);
            result.put("message", "无效的 token，请先登录");
            return result;
        }
        // 上传数据
        boolean success = examService.upload(user, score, examTime);
        if (success) {
            result.put("code", 200);
            result.put("message", "上传成功");
        } else {
            result.put("code", 500);
            result.put("message", "上传失败");
        }
        return result;
    }



    // 获取排行榜接口（不需要登录）
    @GetMapping("/ranking")
    public Map<String, Object> ranking() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> list = examService.getRanking();
        result.put("code", 200);
        result.put("data", list);
        return result;
    }
}
