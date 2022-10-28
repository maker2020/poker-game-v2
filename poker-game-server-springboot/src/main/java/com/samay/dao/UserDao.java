package com.samay.dao;

import org.apache.ibatis.annotations.Param;

import com.samay.game.entity.User;

public interface UserDao {
    
    boolean register(User user);
    User findUserByID(@Param("id")String userID);

}
