package com.samay.netty.handler.aop.test;

import java.util.List;
import java.util.stream.Collectors;

import com.samay.game.Game;
import com.samay.game.entity.Player;
import com.samay.game.entity.Poker;
import com.samay.game.entity.Room;
import com.samay.game.enums.ActionEnum;
import com.samay.game.vo.Notification;

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

    /**
     * 调用于游戏结束之后，摊牌(即将剩余手牌作为notification)用于公示
     * @param room
     */
    public static void showdown(Game game){
        List<Player> players=game.getPlayers();
        for(Player p:players){
            if(p.getPokers()!=null && p.getPokers().size()>0){
                // 将拷贝的牌作为notification
                List<Poker> originPokers=p.getPokers();
                List<Poker> pokers=originPokers.stream().collect(Collectors.toList());
                p.setNotification(new Notification(ActionEnum.PUT, true, pokers));
            }
        }
    }

}
