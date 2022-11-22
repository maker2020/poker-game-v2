package com.samay.netty.handler.utils;

import java.util.List;

import com.samay.game.entity.Player;
import com.samay.game.entity.Room;

/**
 * 房间有关统一处理的工具类
 */
public class RoomUtil {
    
    /**
     * 清除操作记录(例如: 地主争夺出来之后清除、加注结束等情况下的清除)
     */
    public static void clearPlayerNotification(Room room){
        List<Player> players=room.getPlayers();
        for(Player p:players){
            p.setNotification(null);
        }
    }

}
