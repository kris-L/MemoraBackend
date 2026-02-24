package com.example.demo.mapper;

import com.example.demo.entity.User;
import org.apache.ibatis.annotations.*;
import java.util.Date;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO user(nickname, phone, email, password) VALUES(#{nickname}, #{phone}, #{email}, #{password})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Select("SELECT * FROM user WHERE phone = #{phone}")
    User findByPhone(String phone);

    @Select("SELECT * FROM user WHERE email = #{email}")
    User findByEmail(String email);

    @Select("SELECT * FROM user WHERE nickname = #{nickname}")
    User findByNickname(String nickname);

    @Select("SELECT * FROM user WHERE token = #{token}")
    User findByToken(String token);

    @Update("UPDATE user SET token = #{token}, token_expire = #{expire} WHERE id = #{id}")
    int updateToken(@Param("id") Integer id, @Param("token") String token, @Param("expire") Date expire);

    @Delete("DELETE FROM user WHERE id = #{id}")
    int deleteById(Integer id);

    @Update("UPDATE user SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Integer id, @Param("password") String password);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Integer id);

    @Update("UPDATE user SET nickname = #{nickname} WHERE id = #{id}")
    int updateNickname(@Param("id") Integer id, @Param("nickname") String nickname);

    @Update("UPDATE user SET phone = #{phone} WHERE id = #{id}")
    int updatePhone(@Param("id") Integer id, @Param("phone") String phone);

    @Update("UPDATE user SET email = #{email} WHERE id = #{id}")
    int updateEmail(@Param("id") Integer id, @Param("email") String email);
}