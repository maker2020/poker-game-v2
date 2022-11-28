package com.samay.service;

import java.util.List;

import com.samay.game.entity.Item;
import com.samay.game.enums.GameItems;

public interface ItemService {
    List<Item> listItems(String userID);
    boolean decreaseItem(String userID,GameItems item);
}
