package com.samay.netty.handler.aop.test;

import java.util.List;

import com.samay.game.Game;
import com.samay.game.entity.Player;
import com.samay.game.entity.Room;

/**
 * Notification数据操作有关的工具类
 */
public class NotificationUtil {
    
    /**
     * 调用在setActingPlayerAfter之后的方法，用于更新player的notification
     * @param game
     */
    public static void setActingPlayerAfter(Game game){
        // 轮到某个玩家act,则清除其notification
        List<Player> players=game.getPlayers();
        for(Player p:players){
            if(p.getId().equals(game.getActingPlayer())){
                p.setNotification(null);
                break;
            }
        }
    }

    /**
     * 调用于地主争夺出来、加注结束等情况之后的方法，用来清除操作记录(例如: 清除、下的清除)
     */
    public static void clearPlayerNotification(Room room){
        List<Player> players=room.getPlayers();
        for(Player p:players){
            p.setNotification(null);
        }
    }

}
