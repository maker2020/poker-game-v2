package com.samay.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.samay.game.entity.Item;

public interface ItemDao {
    
    List<Item> listItems(@Param("id")String userID);

}
