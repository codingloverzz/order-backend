package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {

    @Select("select * from user where openid=#{openid}")
    User getByOpenid(String openid);

    void inster(User user);

    @Select("select count(*) from user where create_time<#{dayEnd}")
    Long getSumByCreateTime(LocalDateTime dayEnd);

    @Select("select count(*) from user where create_time>=#{dayStart} and create_time<=#{dayEnd}")

    Long getSumByTime(LocalDateTime dayStart, LocalDateTime dayEnd);
}
