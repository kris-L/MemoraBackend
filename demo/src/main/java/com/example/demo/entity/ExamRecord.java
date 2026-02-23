package com.example.demo.entity;

import java.util.Date;

public class ExamRecord {
    private Integer id;
    private Integer userId;     // 用户ID
    private Integer score;      // 分数
    private Date examTime;      // 考试时间
    private Date createdAt;     // 记录创建时间

    // getter 和 setter 也自动生成，这里省略，你自己生成一下
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Date getExamTime() {
        return examTime;
    }

    public void setExamTime(Date examTime) {
        this.examTime = examTime;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
