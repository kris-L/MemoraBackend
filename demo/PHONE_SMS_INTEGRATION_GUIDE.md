# 手机号验证码功能预留设计说明

## 概述
本文档说明了手机号验证码功能的预留设计方案，为将来集成真实短信服务做好准备。

## 当前实现状态

### 1. 核心组件已完成
- ✅ `VerificationCodeService` - 验证码服务层，支持手机和邮箱验证码
- ✅ `SmsUtil` - 短信工具类（预留）
- ✅ 数据库表 `verification_code` 已建立
- ✅ 相关Mapper和Entity已完善

### 2. 接口功能
- ✅ 发送注册手机验证码：`POST /api/auth/send-register-phone-code`
- ✅ 发送绑定手机验证码：`POST /api/auth/send-phone-code`  
- ✅ 手机绑定接口（带验证码验证）：`PUT /api/auth/phone`

## 验证码类型定义

| 类型值 | 用途 |
|-------|------|
| 1 | 注册邮箱验证码 |
| 2 | 重置密码邮箱验证码 |
| 3 | 绑定手机验证码 |
| 4 | 绑定邮箱验证码 |
| 5 | 注册手机验证码 |

## 集成真实短信服务步骤

### 1. 选择短信服务商
推荐服务商：
- 阿里云短信服务
- 腾讯云短信服务
- 华为云短信服务
- 云片短信

### 2. 修改 SmsUtil 类
在 `SmsUtil.java` 中取消TODO注释，集成具体SDK：

```java
// 阿里云短信集成示例
@Autowired
private com.aliyuncs.DefaultAcsClient aliyunSmsClient;

public void sendVerificationCode(String phone, String code, String purpose) {
    try {
        com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest request = 
            new com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest();
        request.setPhoneNumbers(phone);
        request.setSignName("您的签名名称");
        request.setTemplateCode("SMS_XXXXXXXXX"); // 您的模板CODE
        request.setTemplateParam("{\"code\":\"" + code + "\"}");
        
        com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse response = 
            aliyunSmsClient.getAcsResponse(request);
            
        if (!"OK".equals(response.getCode())) {
            throw new RuntimeException("短信发送失败: " + response.getMessage());
        }
    } catch (Exception e) {
        throw new RuntimeException("短信服务异常", e);
    }
}
```

### 3. 配置文件添加
在 `application.properties` 中添加短信服务配置：

```properties
# 阿里云短信配置
aliyun.sms.access-key-id=your-access-key-id
aliyun.sms.access-key-secret=your-access-key-secret
aliyun.sms.sign-name=您的签名
aliyun.sms.template-code=SMS_XXXXXXXXX
```

### 4. 添加相关依赖
在 `pom.xml` 中添加短信服务SDK依赖：

```xml
<!-- 阿里云短信SDK -->
<dependency>
    <groupId>com.aliyun</groupId>
    <artifactId>dysmsapi20170525</artifactId>
    <version>2.0.24</version>
</dependency>
```

## 测试验证

### 1. 单元测试
运行 `PhoneVerificationTest.java` 进行基础功能测试

### 2. 集成测试
- 测试验证码生成和格式验证
- 测试手机号和邮箱格式识别
- 测试统一发送接口

### 3. 端到端测试
- 注册流程手机验证码测试
- 绑定手机功能测试
- 验证码时效性和唯一性测试

## 安全考虑

### 1. 频率限制
建议实现：
- 同一手机号每分钟最多发送1次
- 同一IP每小时最多发送10次
- 每日发送总量限制

### 2. 验证码安全
- 验证码有效期10分钟
- 验证成功后立即失效
- 同类型验证码唯一有效

### 3. 日志记录
记录关键操作日志：
- 验证码发送记录
- 验证码验证记录
- 异常操作记录

## 扩展建议

### 1. 国际化支持
- 支持国际手机号格式
- 多语言短信模板

### 2. 语音验证码
作为短信验证码的补充方案

### 3. 图形验证码
防止恶意刷短信接口

## 注意事项

1. **成本控制**：短信服务按条计费，需要合理控制发送频率
2. **合规要求**：确保符合当地通信法规要求
3. **用户体验**：验证码发送要有明确的成功/失败反馈
4. **监控告警**：建立短信服务异常监控机制

## 当前模拟模式
目前系统以模拟模式运行，在控制台输出验证码信息，便于开发测试。