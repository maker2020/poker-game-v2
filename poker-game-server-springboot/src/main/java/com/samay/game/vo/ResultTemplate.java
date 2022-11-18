package com.samay.game.vo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.samay.game.entity.Poker;

/**
 * Netty服务器所有返回给前端的VO对象模板类
 */
public class ResultTemplate {
    
    /**
     * 返回地主是谁
     * @param boss
     * @return
     */
    public static ResultVO<?> boss(String boss){
        ResultVO<Map<String,Object>> resultVO=new ResultVO<Map<String,Object>>(0, "boss result");
        Map<String,Object> data=new HashMap<>();
        data.put("boss", boss);
        resultVO.setData(data);
        return resultVO;
    }

    /**
     * 返回地主三分牌(3只)
     * @param pokers
     * @return
     */
    public static ResultVO<?> bossPoker(Collection<Poker> pokers){
        Map<String,Object> data=new HashMap<>();
        data.put("bossPokers",pokers);
        ResultVO<Map<String,Object>> resultVO=new ResultVO<>(0, "boss pokers");
        resultVO.setData(data);
        return resultVO;
    }

}
