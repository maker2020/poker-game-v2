package com.samay.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.samay.game.entity.Item;
import com.samay.game.enums.GameItems;

public interface ItemDao {
    
    List<Item> listItems(@Param("id")String userID);
    boolean decreaseItem(@Param("userid")String userID,@Param("name")GameItems item);

}
