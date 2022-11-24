package com.samay.dao;

import org.apache.ibatis.annotations.Param;

import com.samay.game.entity.Player;
import com.samay.game.entity.User;
import com.samay.game.entity.UserData;

public interface UserDao {
    
    boolean register(User user);
    User findUserByID(@Param("id")String userID);
    Player findPlayerByID(@Param("id")String userID);
    boolean updateUser(User user);
    boolean updateUserData(UserData userData);

}
