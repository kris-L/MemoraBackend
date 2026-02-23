package com.example.demo.service;


import com.example.demo.entity.User;
import com.example.demo.mapper.ExamRecordMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.util.PasswordUtil;
import com.example.demo.util.TokenUtil;
import com.example.demo.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Calendar;
import java.util.Date;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;  // 新增注入

    @Transactional
    public boolean register(String nickname, String phone, String email, String password) {
        // 1. 至少提供一个联系方式
        if (phone == null && email == null) {
            throw new IllegalArgumentException("手机号和邮箱不能同时为空");
        }
        // 2. 格式校验
        if (phone != null && !ValidationUtil.isValidPhone(phone)) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        if (email != null && !ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        if (!ValidationUtil.isValidNickname(nickname)) {
            throw new IllegalArgumentException("昵称格式不正确（1-50位字符）");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            throw new IllegalArgumentException("密码长度至少8位");
        }

        // 3. 唯一性校验
        if (userMapper.findByNickname(nickname) != null) {
            throw new IllegalArgumentException("昵称已被使用");
        }
        if (phone != null && userMapper.findByPhone(phone) != null) {
            throw new IllegalArgumentException("手机号已被注册");
        }
        if (email != null && userMapper.findByEmail(email) != null) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        // 4. 创建用户
        User user = new User();
        user.setNickname(nickname);
        user.setPhone(phone);
        user.setEmail(email);
        user.setPassword(PasswordUtil.encode(password));

        return userMapper.insert(user) > 0;
    }

    public String login(String account, String password) {
        if (account == null || password == null) {
            return null;
        }
        User user = null;
        if (account.contains("@")) {
            user = userMapper.findByEmail(account);
        } else {
            user = userMapper.findByPhone(account);
        }
        if (user != null && PasswordUtil.matches(password, user.getPassword())) {
            String token = TokenUtil.generateToken();
            // 设置过期时间：当前时间 + 30天
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 30);
            Date expire = calendar.getTime();
            userMapper.updateToken(user.getId(), token, expire);
            return token;
        }
        return null;
    }

    public User getUserByToken(String token) {
        User user = userMapper.findByToken(token);
        if (user == null) {
            return null;
        }
        // 检查 token 是否过期
        if (user.getTokenExpire() != null && user.getTokenExpire().before(new Date())) {
            // token 已过期，可以选择自动清除 token（可选）
            // userMapper.updateToken(user.getId(), null, null); // 如果需要清除
            return null;
        }
        return user;
    }

    @Transactional
    public boolean deleteAccount(Integer userId) {
        // 先删除考试记录，再删除用户
        examRecordMapper.deleteByUserId(userId);
        return userMapper.deleteById(userId) > 0;
    }
}