package com.example.demo.service;

import com.example.demo.entity.ExamRecord;
import com.example.demo.entity.User;
import com.example.demo.mapper.ExamRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Service
public class ExamService {

    @Autowired
    private ExamRecordMapper examRecordMapper;

    // 上传考试数据
    public boolean upload(User user, Integer score, String examTime) {
        ExamRecord record = new ExamRecord();
        record.setUserId(user.getId());   // 当前登录用户的 id
        record.setScore(score);
        // 把字符串格式的考试时间转成数据库需要的 Timestamp 格式
        // 注意：前端传来的 examTime 必须像 "2025-01-01 10:30:00" 这样的格式
        record.setExamTime(Timestamp.valueOf(examTime));
        return examRecordMapper.insert(record) > 0;
    }

    // 获取排行榜
    public List<Map<String, Object>> getRanking() {
        return examRecordMapper.getRanking();
    }
}