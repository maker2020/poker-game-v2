package com.samay.game.entity;

import java.io.Serializable;

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
public class Item implements Serializable{
    
    private static final long serialVersionUID = 1L;

    private String userid;
    private GameItems name;
    private long count;

}
