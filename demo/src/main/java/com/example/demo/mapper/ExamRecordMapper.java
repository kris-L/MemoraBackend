package com.example.demo.mapper;

import com.example.demo.entity.ExamRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ExamRecordMapper {

    @Insert("INSERT INTO exam_record(user_id, score, exam_time) VALUES(#{userId}, #{score}, #{examTime})")
    int insert(ExamRecord record);

    @Select("SELECT u.nickname as username, r.score, r.exam_time " +
            "FROM exam_record r JOIN user u ON r.user_id = u.id " +
            "ORDER BY r.score DESC LIMIT 10")
    List<Map<String, Object>> getRanking();

    @Delete("DELETE FROM exam_record WHERE user_id = #{userId}")
    int deleteByUserId(Integer userId);
}