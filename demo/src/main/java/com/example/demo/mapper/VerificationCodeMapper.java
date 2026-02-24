package com.example.demo.mapper;

import com.example.demo.entity.VerificationCode;
import org.apache.ibatis.annotations.*;

@Mapper
public interface VerificationCodeMapper {

    @Insert("INSERT INTO verification_code(target, code, type, expiry_time, used, created_at) " +
            "VALUES(#{target}, #{code}, #{type}, #{expiryTime}, #{used}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(VerificationCode vc);

    // 查询某个目标最新未使用的验证码（按类型）
    @Select("SELECT * FROM verification_code WHERE target = #{target} AND type = #{type} " +
            "AND used = 0 AND expiry_time > NOW() ORDER BY id DESC LIMIT 1")
    VerificationCode findLatestUnused(@Param("target") String target, @Param("type") Integer type);

    // 将验证码标记为已使用
    @Update("UPDATE verification_code SET used = 1 WHERE id = #{id}")
    int markAsUsed(Integer id);

    // 使某个目标的所有验证码失效（可选）
    @Update("UPDATE verification_code SET used = 1 WHERE target = #{target} AND type = #{type}")
    int invalidateAll(@Param("target") String target, @Param("type") Integer type);
}