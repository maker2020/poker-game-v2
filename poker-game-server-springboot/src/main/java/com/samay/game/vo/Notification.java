package com.samay.game.vo;

import com.samay.game.enums.ActionEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * group通知的消息
 * <p>
 * 字段通常为
 * <ul>
 * <li>type(action)</li>
 * <li>choice(tendency)</li>
 * <li>playerID</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    private ActionEnum type;
    private boolean choice;
    /**
     * user唯一标识(可以是username、id等)
     */
    private String playerID;

    public Notification(String playerID){
        this.playerID=playerID;
    }

    public Notification(String playerID, ActionEnum type) {
        this.playerID = playerID;
        this.type = type;
    }

}