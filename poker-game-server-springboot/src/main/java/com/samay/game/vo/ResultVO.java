package com.samay.game.vo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.samay.game.entity.Player;
import com.samay.game.entity.Poker;
import com.samay.game.entity.Room;
import com.samay.game.enums.ActionEnum;

/**
 * 服务器所有返回给前端的View Object模板类
 */
public class ResultVO {
    
    /**
     * 返回叫地主结果
     * @param boss 地主玩家唯一标识(name/id)
     * @return
     */
    public static Map<String,Object> resultMap(String boss){
        Map<String,Object> map=new HashMap<>();
        map.put("boss", boss);
        return map;
    }

    /**
     * 返回地主三分牌
     * @param pokers
     * @return
     */
    public static Map<String,Object> resultMap(Collection<Poker> pokers){
        Map<String,Object> map=new HashMap<>();
        map.put("bossPokers",pokers);
        return map;
    }

    /**
     * 返回房间信息
     * @param room
     * @return
     */
    public static Map<String,Object> resultMap(Room room){
        Map<String,Object> map=new HashMap<>();
        List<Map<String,Object>> playerStatusList=new ArrayList<>();
        for(int i=0;i<room.getPlayers().size();i++){
            Player p=room.getPlayers().get(i);
            // 玩家座位信息（即玩家个人信息）...
            Map<String,Object> playerStatusMap=new HashMap<>();
            playerStatusMap.put("playerID", p.getName());
            playerStatusMap.put("ready", p.isReady());
            playerStatusMap.put("sex", p.getSex());
            playerStatusMap.put("nickName", p.getNickName());
            playerStatusList.add(playerStatusMap);
        }
        map.put("playerStatus", playerStatusList);
        map.put("roomID", room.getId());
        return map;
    }

    /**
     * @param user 玩家唯一标识(playerID)
     * @param ready
     * @return
     */
    public static Map<String,Object> resultMap(String user,boolean ready){
        Map<String,Object> map=new HashMap<>();
        map.put("playerID", user);
        map.put("ready", ready);
        return map;
    }

    /**
     * @param user 玩家唯一标识(playerID)
     * @param pokers
     * @return
     */
    public static Map<String,Object> resultMap(String user,List<Poker> pokers){
        Map<String,Object> map=new HashMap<>();
        map.put("playerID", user);
        map.put("pokers", pokers);
        return map;
    }

    /**
     * 
     * @param action 标识下一个行为
     * @param turn 下一个轮到谁:玩家唯一标识(playerID)
     * @param notification 广播通知
     * @return
     */
    public static Map<String,Object> resultMap(ActionEnum action,String turn,Notification notification){
        Map<String,Object> map=new HashMap<>();
        map.put("action", action.getAction());
        map.put("turn", turn);
        map.put("notification", notification);
        return map;
    }

    /**
     * 出牌VO
     * @param action
     * @param turn 玩家唯一标识(playerID)
     * @param notification
     * @param putPokers
     * @return
     */
    public static Map<String,Object> resultMap(ActionEnum action,String turn,Notification notification,List<Poker> putPokers,int restPokerNum){
        Map<String,Object> map=new HashMap<>();
        map.put("action", action.getAction());
        map.put("turn", turn);
        map.put("notification", notification);
        map.put("putPokers", putPokers);
        map.put("restPokerNum", restPokerNum);
        return map;
    }

    /**
     * 更新vo对象的notification
     * @param turn 玩家唯一标识(playerID)
     * @param resultVO
     * @param notification
     */
    public static void updateResultMap(Map<String,Object> resultVO,ActionEnum action,String turn){
        resultVO.put("action", action.getAction());
        resultVO.put("turn", turn);
    }

    /**
     * 游戏结果对象: 可以包含很多内容，例如胜利方、分数计算等等
     * @param winnerIdList
     */
    public static Map<String,Object> gameResult(List<String> winnerIdList){
        Map<String,Object> map=new HashMap<>();
        map.put("winners", winnerIdList);
        return map;
    }

    /**
     * 操作行为失败
     * @return
     */
    public static Map<String,Object> actionFail(ActionEnum action){
        Map<String,Object> map=new HashMap<>();
        map.put("fail", true); // 后续添加失败原因
        map.put("action", action);
        return map;
    }

}
