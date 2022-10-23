package com.samay.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.samay.dao.ItemDao;
import com.samay.game.entity.Item;
import com.samay.service.ItemService;

/**
 * 游戏道具业务类
 */
@Service
public class ItemServiceImpl implements ItemService{

    @Autowired
    private ItemDao itemDao;

    /**
     * 根据用户id查找道具list
     */
    @Override
    public List<Item> listItems(String userID) {
        List<Item> list=itemDao.listItems(userID);
        return list;
    }
    
}
