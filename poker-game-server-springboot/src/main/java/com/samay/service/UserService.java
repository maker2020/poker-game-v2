package com.samay.service;

import com.samay.game.entity.User;

public interface UserService {
    
    boolean register(User user);
    User findUserById(String userID);

}
