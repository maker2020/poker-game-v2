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
            playerStatusMap.put("playerID", p.getId());
            playerStatusMap.put("ready", p.isReady());
            playerStatusMap.put("sex", p.getSex());
            playerStatusMap.put("nickName", p.getNickName());
            playerStatusMap.put("freeMoney", p.getFreeMoney());
            playerStatusList.add(playerStatusMap);
        }
        map.put("playerStatus", playerStatusList);
        map.put("roomID", room.getId());
        map.put("multiple", room.getGame().getMultiple());
        map.put("baseScore", room.getGame().getBaseScore());
        return map;
    }

    /**
     * 某某id，出了xx牌
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
     * 操作轮询
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
        resultVO.put("action", action==null?null:action.getAction());
        resultVO.put("turn", turn);
    }

    /**
     * 游戏结果对象: 可以包含很多内容，例如胜利方、分数计算等等
     * @param winnerIdList
     */
    public static Map<String,Object> gameResult(Map<String,Object> result){
        // 其他数据
        return result;
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

    /**
     * 展示当前对局倍数
     * @param multiple
     * @return
     */
    public static Map<String,Object> multiplying(int multiple){
        Map<String,Object> map=new HashMap<>();
        map.put("multiple", multiple);
        return map;
    }

    /**
     * 展示加倍情况（包含了当前对局倍数结果）
     * @param notification
     * @param multiple
     * @return
     */
    public static Map<String,Object> multipleResultMap(Notification notification,int multiple){
        Map<String,Object> map=new HashMap<>();
        map.put("notification", notification);
        Map<String,Object> map2=multiplying(multiple);
        map.putAll(map2);
        return map;
    }

    /**
     * 通知各玩家开始进行加注
     * @param done 是否完成该阶段
     * @return
     */
    public static Map<String,Object> raiseStatus(boolean done){
        Map<String,Object> map=new HashMap<>();
        map.put("action", ActionEnum.MULTIPLE.getAction());
        map.put("turnAll", true);
        map.put("done", done);
        return map;
    }

    /**
     * 返回出牌提示信息
     * @return
     */
    public static Map<String,Object> tipResult(List<Poker> pokers,boolean exist){
        Map<String,Object> map=new HashMap<>();
        map.put("tipPokers", pokers);
        map.put("exist", exist);
        map.put("tipMsg", "none");
        return map;
    }

}
