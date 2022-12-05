package com.samay.game.vo;

import java.io.Serializable;
import java.util.List;

import com.samay.game.bo.Poker;
import com.samay.game.enums.ActionEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 记录玩家的操作类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    // 基本字段
    private ActionEnum type;
    private boolean choice;

    public Notification(ActionEnum type,boolean choice){
        this.type=type;
        this.choice=choice;
    }

    // 额外字段
    private List<Poker> putPokers;

}