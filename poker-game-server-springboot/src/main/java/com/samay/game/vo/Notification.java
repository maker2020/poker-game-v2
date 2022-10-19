package com.samay.game.vo;

import com.samay.game.enums.ActionEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * group通知的消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification{
    
    private ActionEnum type;
    private boolean choice;
    /**
     * user唯一标识(可以是username、id等)
     */
    private String user;

}