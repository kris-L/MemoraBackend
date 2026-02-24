package com.example.demo.entity;

import java.util.Date;

public class VerificationCode {
    private Integer id;
    private String target;      // 邮箱或手机号
    private String code;        // 6位数字
    private Integer type;       // 1注册 2重置密码 3绑定手机 4绑定邮箱 ...
    private Date expiryTime;
    private Integer used;       // 0未使用 1已使用
    private Date createdAt;

    // getter/setter 方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Date getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Date expiryTime) {
        this.expiryTime = expiryTime;
    }

    public Integer getUsed() {
        return used;
    }

    public void setUsed(Integer used) {
        this.used = used;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}