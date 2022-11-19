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
 * Netty服务器所有返回给前端的VO对象模板类
 */
public class RV {
    
    /**
     * 返回地主是谁
     * @param boss
     * @return
     */
    public static ResultVO<Map<String,Object>> boss(String boss){
        Map<String,Object> data=new HashMap<>();
        data.put("boss", boss);
        return new ResultVO<>(0, "boss result",data);
    }

    /**
     * 返回地主三分牌(3只)
     * @param pokers
     * @return
     */
    public static ResultVO<Map<String,Object>> bossPoker(Collection<Poker> pokers){
        Map<String,Object> data=new HashMap<>();
        data.put("bossPokers",pokers);
        ResultVO<Map<String,Object>> resultVO=new ResultVO<>(0, "boss pokers",data);
        return resultVO;
    }

    /**
     * 返回游戏房间信息
     * @param room
     * @return
     */
    public static ResultVO<Map<String,Object>> roomInfo(Room room){
        Map<String,Object> data=new HashMap<>();
        List<Map<String,Object>> playerInfoList=new ArrayList<>();
        for(int i=0;i<room.getPlayers().size();i++){
            Player p=room.getPlayers().get(i);
            // 玩家座位信息（即玩家个人信息）...
            Map<String,Object> playerStatusMap=new HashMap<>();
            playerStatusMap.put("playerID", p.getId());
            playerStatusMap.put("ready", p.isReady());
            playerStatusMap.put("sex", p.getSex());
            playerStatusMap.put("nickName", p.getNickName());
            playerStatusMap.put("freeMoney", p.getFreeMoney());
            playerInfoList.add(playerStatusMap);
        }
        data.put("playerStatus", playerInfoList);
        data.put("roomID", room.getId());
        data.put("multiple", room.getGame().getMultiple());
        data.put("baseScore", room.getGame().getBaseScore());
        ResultVO<Map<String,Object>> resultVO=new ResultVO<>(0, "room info", data);
        return resultVO;
    }

    /**
     * 返回玩家(id)收到系统发的牌的结果
     * @param user
     * @param pokers
     * @return
     */
    public static ResultVO<Map<String,Object>> handoutResult(String user,List<Poker> pokers){
        Map<String,Object> data=new HashMap<>();
        data.put("playerID", user);
        data.put("pokers", pokers);
        ResultVO<Map<String,Object>> resultVO=new ResultVO<>(0, "xx出了xx牌",data);
        return resultVO;
    }

    /**
     * 返回轮询消息（轮到xx做action+notification)
     * @param actionEnum
     * @param turn
     * @param notification
     * @return
     */
    public static ResultVO<Map<String,Object>> actionTurn(ActionEnum actionEnum,String turn,Notification notification){
        Map<String,Object> data=new HashMap<>();
        data.put("action", actionEnum.getAction());
        data.put("turn", turn);
        data.put("notification", notification);
        ResultVO<Map<String,Object>> resultVO=new ResultVO<>(0, "轮到xx干action,外加上一个操作通知", data);
        return resultVO;
    }

    public static ResultVO<Map<String,Object>> putResult(ActionEnum actionEnum,String turn,Notification notification,List<Poker> putPokers,int restPokerNum){
        Map<String,Object> data=new HashMap<>();
        data.put("action", actionEnum.getAction());
        data.put("turn", turn);
        data.put("notification", notification);
        data.put("putPokers", putPokers);
        data.put("restPokerNum", restPokerNum);
        ResultVO<Map<String,Object>> resultVO=new ResultVO<>(0,"玩家出了xx牌，剩余多少牌，以及轮询消息",data);
        return resultVO;
    }

    /**
     * 更新actionTurn VO的action和turn字段
     * @param resultVO
     * @param action
     * @param turn
     */
    public static void updateActionTurn(ResultVO<Map<String,Object>> resultVO,ActionEnum action,String turn){
        Map<String,Object> data=resultVO.getData();
        data.put("action", action==null?null:action.getAction());
        data.put("turn", turn);
    }

    /**
     * 返回游戏结果对象: 包含胜利、分数等等
     * @param data
     * @return
     */
    public static ResultVO<Map<String,Object>> gameResult(Map<String,Object> data){
        // 其他数据
        return new ResultVO<>(0, "游戏结算相关数据", data);
    }

    /**
     * 返回操作失败的信息
     * @param action
     * @return
     */
    public static ResultVO<Map<String,Object>> actionFail(ActionEnum action){
        Map<String,Object> data=new HashMap<>();
        data.put("fail", true); // 后续添加失败原因
        data.put("action", action);
        return new ResultVO<>(-1, "操作失败", data);
    }

    /**
     * 展示当前对局倍数
     * @param multiple
     * @return
     */
    public static ResultVO<Map<String,Object>> multipleInfo(int multiple){
        Map<String,Object> data=new HashMap<>();
        data.put("multiple", multiple);
        return new ResultVO<>(0, "当前对局倍数", data);
    }

    /**
     * 返回加倍情况（包含了当前对局倍数结果）
     * @param notification
     * @param multiple
     * @return
     */
    public static ResultVO<Map<String,Object>> multipleResult(Notification notification,int multiple){
        Map<String,Object> data=new HashMap<>();
        data.put("notification", notification);
        ResultVO<Map<String,Object>> vo=multipleInfo(multiple);
        data.putAll(vo.getData());
        return new ResultVO<>(0, "返回加倍情况", data);
    }

    /**
     * 返回 通知玩家开始加注的信息
     * @param done 是否完成了加注阶段
     * @return
     */
    public static ResultVO<Map<String,Object>> raiseStatus(boolean done){
        Map<String,Object> data=new HashMap<>();
        data.put("action", ActionEnum.MULTIPLE);
        data.put("turnAll", true);
        data.put("done", done);
        return new ResultVO<>(0,"通知玩家处于加注阶段",data);
    }

    /**
     * 返回 出牌提示按钮给出的 提示信息
     * @param pokers
     * @param exist
     * @return
     */
    public static ResultVO<Map<String,Object>> tipResult(List<Poker> pokers,boolean exist){
        Map<String,Object> data=new HashMap<>();
        data.put("tipPokers", pokers);
        data.put("exist", exist);
        data.put("tipMsg", "none");
        return new ResultVO<Map<String,Object>>(0, "返回服务器给出的出牌提示", data);
    }

}
