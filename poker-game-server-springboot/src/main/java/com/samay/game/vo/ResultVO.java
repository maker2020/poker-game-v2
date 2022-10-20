package com.samay.game.vo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.samay.game.entity.Poker;
import com.samay.game.enums.ActionEnum;

/**
 * 服务器所有返回给前端的View Object模板类
 */
public class ResultVO {
    
    /**
     * 返回叫地主结果
     * @param boss 地主用户唯一标识(name/id)
     * @return
     */
    public static Map<String,Object> resultMap(String boss){
        Map<String,Object> map=new HashMap<>();
        map.put("boss", boss);
        return map;
    }

    /**
     * 
     * @param user 用户唯一标识(name/id)
     * @param players 用户唯一标识数组
     * @param roomID
     * @return
     */
    public static Map<String,Object> resultMap(String user,String roomID,String[] players){
        Map<String,Object> map=new HashMap<>();
        map.put("user", user);
        map.put("roomID", roomID);
        map.put("players",players);
        return map;
    }

    /**
     * @param user 用户唯一标识(name/id)
     * @param ready
     * @return
     */
    public static Map<String,Object> resultMap(String user,boolean ready){
        Map<String,Object> map=new HashMap<>();
        map.put("user", user);
        map.put("ready", ready);
        return map;
    }

    /**
     * @param user 用户唯一标识(name/id)
     * @param pokers
     * @return
     */
    public static Map<String,Object> resultMap(String user,List<Poker> pokers){
        Map<String,Object> map=new HashMap<>();
        map.put("user", user);
        map.put("pokers", pokers);
        return map;
    }

    /**
     * 
     * @param action 标识下一个行为
     * @param turn 下一个轮到谁
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
     * @param turn
     * @param notification
     * @param putPokers
     * @return
     */
    public static Map<String,Object> resultMap(ActionEnum action,String turn,Notification notification,List<Poker> putPokers){
        Map<String,Object> map=new HashMap<>();
        map.put("action", action.getAction());
        map.put("turn", turn);
        map.put("notification", notification);
        map.put("putPokers", putPokers);
        return map;
    }

    /**
     * 更新vo对象的notification
     * @param resultVO
     * @param notification
     */
    public static void updateResultMap(Map<String,Object> resultVO,ActionEnum action,String turn){
        resultVO.put("action", action.getAction());
        resultVO.put("turn", turn);
    }

}
