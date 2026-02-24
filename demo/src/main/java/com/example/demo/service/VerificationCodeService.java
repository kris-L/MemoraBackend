package com.example.demo.service;

import com.example.demo.entity.VerificationCode;
import com.example.demo.mapper.VerificationCodeMapper;
import com.example.demo.util.CodeUtil;
import com.example.demo.util.MailUtil;
import com.example.demo.util.SmsUtil; // 预留短信工具类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class VerificationCodeService {

    @Autowired
    private VerificationCodeMapper codeMapper;

    @Autowired
    private MailUtil mailUtil;
    
    @Autowired(required = false) // 可选依赖，因为短信功能可能尚未实现
    private SmsUtil smsUtil;

    // 发送邮箱验证码
    public void sendEmailCode(String email, Integer type, String purpose) {
        // 1. 生成6位数字验证码
        String code = CodeUtil.generate6DigitCode();
        // 2. 设置过期时间（10分钟后）
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 10);
        Date expiry = cal.getTime();

        // 3. 先使该邮箱同类型的旧验证码失效（可选，避免多个有效验证码）
        codeMapper.invalidateAll(email, type);

        // 4. 存入数据库
        VerificationCode vc = new VerificationCode();
        vc.setTarget(email);
        vc.setCode(code);
        vc.setType(type);
        vc.setExpiryTime(expiry);
        vc.setUsed(0);
        codeMapper.insert(vc);

        // 5. 发送邮件
        mailUtil.sendVerificationCode(email, code, purpose);
    }

    // 验证验证码（检查是否存在且未过期且未使用）
    public boolean verifyCode(String target, Integer type, String inputCode) {
        System.out.println("[安全审计] 验证码验证开始 - 目标: " + target + ", 类型: " + type + ", 输入: " + inputCode);
        
        VerificationCode vc = codeMapper.findLatestUnused(target, type);
        if (vc == null) {
            System.out.println("[安全警告] 验证失败 - 未找到有效验证码");
            return false;
        }
        
        System.out.println("[安全审计] 数据库验证码 - 代码: " + vc.getCode() + ", 过期: " + vc.getExpiryTime() + ", 已使用: " + vc.getUsed());
        
        // 严格验证所有条件
        if (!vc.getCode().equals(inputCode)) {
            System.out.println("[安全警告] 验证失败 - 验证码不匹配");
            return false;
        }
        
        if (vc.getExpiryTime().before(new Date())) {
            System.out.println("[安全警告] 验证失败 - 验证码已过期");
            return false;
        }
        
        if (vc.getUsed() != 0) {
            System.out.println("[安全警告] 验证失败 - 验证码已被使用");
            return false;
        }
        
        // 验证成功后立即标记为已使用
        System.out.println("[安全审计] 验证成功，标记为已使用");
        codeMapper.markAsUsed(vc.getId());
        return true;
    }

    // 8. 发送手机验证码（预留功能）
    public void sendPhoneCode(String phone, Integer type, String purpose) {
        // 1. 生成6位数字验证码
        String code = CodeUtil.generate6DigitCode();
        // 2. 设置过期时间（10分钟后）
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 10);
        Date expiry = cal.getTime();

        // 3. 先使该手机号同类型的旧验证码失效
        codeMapper.invalidateAll(phone, type);

        // 4. 存入数据库
        VerificationCode vc = new VerificationCode();
        vc.setTarget(phone);
        vc.setCode(code);
        vc.setType(type);
        vc.setExpiryTime(expiry);
        vc.setUsed(0);
        codeMapper.insert(vc);

        // 5. 发送短信（预留，当前为模拟实现）
        if (smsUtil != null) {
            smsUtil.sendVerificationCode(phone, code, purpose);
        } else {
            // 模拟发送成功（实际部署时需要集成真实短信服务）
            System.out.println("【模拟】向手机 " + phone + " 发送验证码: " + code + "，用途: " + purpose);
        }
    }

    // 判断是否为手机号
    public boolean isPhoneNumber(String target) {
        return target != null && target.matches("^1[3-9]\\d{9}$");
    }

    // 判断是否为邮箱
    public boolean isEmail(String target) {
        return target != null && target.contains("@");
    }

    // 统一的验证码发送方法
    public void sendCode(String target, Integer type, String purpose) {
        if (isPhoneNumber(target)) {
            sendPhoneCode(target, type, purpose);
        } else if (isEmail(target)) {
            sendEmailCode(target, type, purpose);
        } else {
            throw new IllegalArgumentException("不支持的目标类型: " + target);
        }
    }
}