package com.samay.game.entity;

import com.samay.game.enums.GameItems;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity(user_item)<p>
 * 游戏道具表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"userid","name"})
public class Item {
    
    private String userid;
    private GameItems name;
    private long count;

}
