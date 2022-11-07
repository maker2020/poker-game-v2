package com.samay.game.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.samay.game.dto.ReqBossDTO;
import com.samay.game.entity.Player;
import com.samay.game.enums.ActionEnum;
import com.samay.netty.handler.holder.ChannelHolder;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;


/**
 * <b>时间管理相关工具</b>
 * <p>
 * 例: 限时30s完成操作
 */
@Slf4j
public class TimerUtil {

    private static long time=30;
    private static TimeUnit timeUnit=TimeUnit.SECONDS;

    private static Map<String,Object> playerAct=new HashMap<>();

    /**
     * <b>指定操作类型的时间监测</b>
     * <p>即在一定的时间内没有检测到玩家发起的相关action请求，系统将按照默认行为方式处理
     * 
     * @param action 什么操作
     * @param ch 通道
     */
    public static void checkTimeout(ActionEnum action, Channel ch) throws Exception {
        Player p=ChannelHolder.attrPlayer(ch);
        if(playerAct.get(p.getId())==null){
            playerAct.put(p.getId(), new Object());
            ExecutorService exec=Executors.newFixedThreadPool(2);
            Future<?> future=exec.submit(()->{
                synchronized(playerAct.get(p.getId())){
                    playerAct.get(p.getId()).wait();
                }
                return p.getId();
            });
            exec.submit(()->{
                try {
                    future.get(time, timeUnit);                
                } catch (Exception e) {
                    if(e instanceof TimeoutException){
                        // 根据action，做出默认操作
                        defaultAction(action, ch);
                    }else{
                        log.error("超时检测异常", e);
                    }
                }
            });
        }else{
            synchronized(playerAct.get(p.getId())){
                playerAct.get(p.getId()).notify();
            }
            playerAct.put(p.getId(), null);
        }
    }

    private static void defaultAction(ActionEnum actionEnum,Channel ch){
        if(actionEnum==ActionEnum.CALL){
            ReqBossDTO reqBossDTO=new ReqBossDTO();
            reqBossDTO.setAction("call");
            reqBossDTO.setTendency(false);
            ch.pipeline().fireChannelRead(reqBossDTO);
        }else if(actionEnum==ActionEnum.ASK){
            ReqBossDTO reqBossDTO=new ReqBossDTO();
            reqBossDTO.setAction("ask");
            reqBossDTO.setTendency(false);
            ch.pipeline().fireChannelRead(reqBossDTO);
        }
    }

}
