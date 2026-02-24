package com.example.demo.util;

import org.springframework.stereotype.Component;

/**
 * 短信工具类（预留）
 * 实际使用时需要集成具体的短信服务商SDK，如阿里云短信、腾讯云短信等
 */
@Component
public class SmsUtil {

    /**
     * 发送验证码短信
     * @param phone 手机号
     * @param code 验证码
     * @param purpose 用途说明
     */
    public void sendVerificationCode(String phone, String code, String purpose) {
        // TODO: 集成真实的短信服务商SDK
        // 示例伪代码：
        /*
        // 阿里云短信示例
        com.aliyuncs.DefaultAcsClient client = new com.aliyuncs.DefaultAcsClient(profile);
        com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest request = new com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest();
        request.setPhoneNumbers(phone);
        request.setSignName("您的签名");
        request.setTemplateCode("SMS_XXXXXX");
        request.setTemplateParam("{\"code\":\"" + code + "\"}");
        com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse response = client.getAcsResponse(request);
        */

        // 腾讯云短信示例
        /*
        com.tencentcloudapi.sms.v20210111.SmsClient client = new com.tencentcloudapi.sms.v20210111.SmsClient(cred, "", profile);
        com.tencentcloudapi.sms.v20210111.models.SendSmsRequest req = new com.tencentcloudapi.sms.v20210111.models.SendSmsRequest();
        req.setPhoneNumberSet(new String[]{"+86" + phone});
        req.setTemplateID("XXXXXX");
        req.setSign("您的签名");
        req.setTemplateParamSet(new String[]{code});
        com.tencentcloudapi.sms.v20210111.models.SendSmsResponse resp = client.SendSms(req);
        */

        // 当前为模拟实现
        System.out.println("【短信服务】向手机 " + phone + " 发送验证码: " + code + "，用途: " + purpose);
        System.out.println("注意：请在此处集成真实的短信服务商SDK");
    }

    /**
     * 发送通知类短信
     * @param phone 手机号
     * @param message 消息内容
     */
    public void sendNotification(String phone, String message) {
        // TODO: 实现通知类短信发送
        System.out.println("【短信通知】向手机 " + phone + " 发送消息: " + message);
    }
}