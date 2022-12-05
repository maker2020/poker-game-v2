package com.samay.game.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.samay.game.bo.Poker;
import com.samay.game.bo.Room;
import com.samay.game.enums.ActionEnum;
import com.samay.game.vo.ResultVO;

/**
 * Netty服务器所有返回给前端的VO对象模板类
 */
public class RV {
    
    /**
     * 房间内所有与游戏相关的数据<p>
     * (同时针对玩家过滤掉敏感数据：如只能看到自己的手牌。)
     * 
     * @return
     */
    public static ResultVO<Map<String,Object>> roomData(Room room){
        ResultVO<Map<String,Object>> resultVO=new ResultVO<>(0, "room game data");
        Map<String,Object> data=new HashMap<>();
        data.put("room", room);
        data.put("game", room.getGame());
        resultVO.setData(data);
        return resultVO;
    }

    /**
     * 返回游戏结果对象: 包含胜利、分数等等
     * @param data
     * @return
     */
    public static ResultVO<Map<String,Object>> gameResult(Map<String,Object> data){
        // 其他数据
        return new ResultVO<>(3, "游戏结算相关数据", data);
    }

    /**
     * 返回操作失败的信息
     * @param action
     * @return
     */
    public static ResultVO<Map<String,Object>> actionFail(ActionEnum action){
        Map<String,Object> data=new HashMap<>();
        data.put("fail", true); // 可添加失败原因
        data.put("action", action);
        return new ResultVO<>(-1, "操作失败", data);
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
        data.put("existTip", exist);
        return new ResultVO<Map<String,Object>>(2, "返回服务器给出的出牌提示", data);
    }

}
