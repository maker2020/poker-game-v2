package com.samay.service;

import com.samay.game.bo.Player;
import com.samay.game.entity.User;

public interface UserService {
    
    boolean register(User user);
    User findUserById(String userID);
    Player findPlayerByID(String userID);
    boolean updateUser(User user);
    boolean updatePlayer(Player player);

}
