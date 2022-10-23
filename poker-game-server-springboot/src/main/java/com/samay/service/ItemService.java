package com.samay.service;

import java.util.List;

import com.samay.game.entity.Item;

public interface ItemService {
    List<Item> listItems(String userID);
}
