package com.samay.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.samay.dao.UserDao;
import com.samay.game.bo.Player;
import com.samay.game.entity.User;
import com.samay.game.entity.UserData;
import com.samay.service.UserService;

/**
 * <b>用户服务(业务)</b>
 * <p>
 * 包含业务及其逻辑验证、参数校验等处理的方法/服务。
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    /**
     * <b>注册用户</b>
     * <p>
     * 校验交给数据库字段各个约束
     * 
     * @return 返回类型true/false若不够用则改为int，并加入其他dao方法组合类型
     */
    @Override
    public boolean register(User user) {
        return userDao.register(user);
    }

    /**
     * 通过用户id查找用户
     */
    @Override
    public User findUserById(String userID) {
        return userDao.findUserByID(userID);
    }

    @Override
    public boolean updateUser(User user) {
        return userDao.updateUser(user);
    }

    @Override
    public Player findPlayerByID(String userID) {
        return userDao.findPlayerByID(userID);
    }

    @Override
    @Transactional
    public boolean updatePlayer(Player player) {
        User user = player;
        boolean b1 = userDao.updateUser(user);
        UserData userData = new UserData();
        userData.setExp(player.getExp());
        userData.setFreeMoney(player.getFreeMoney());
        userData.setLoseCount(player.getLoseCount());
        userData.setPayMoney(player.getPayMoney());
        userData.setUserid(player.getId());
        userData.setWinCount(player.getWinCount());
        boolean b2 = userDao.updateUserData(userData);
        return b1 && b2;
    }

}
