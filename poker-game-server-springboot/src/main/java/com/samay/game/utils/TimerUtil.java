package com.samay.game.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.samay.game.Game;
import com.samay.game.dto.PutPokerDTO;
import com.samay.game.dto.ReqBossDTO;
import com.samay.game.entity.Player;
import com.samay.game.entity.Poker;
import com.samay.game.entity.Room;
import com.samay.game.enums.ActionEnum;
import com.samay.netty.handler.holder.ChannelHolder;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 * <b>时间管理相关工具</b>
 * <p>
 * 例: 限时xx秒完成操作
 */
@Slf4j
public class TimerUtil {

    private static final long time = 7;
    private static final TimeUnit timeUnit = TimeUnit.SECONDS;

    private static Map<String, Object> playerAct = Collections.synchronizedMap(new HashMap<>());

    /**
     * <b>指定操作类型的时间监测</b>
     * <p>
     * 即在一定的时间内没有检测到玩家发起的相关action请求，系统将按照默认行为方式处理
     * 
     * @param action 什么操作
     * @param ch     通道
     */
    public static void checkTimeout(ActionEnum action, String playerID) throws Exception {
        if (playerID == null || action == null)
            return;
        if (playerAct.get(playerID) == null) {
            log.info("player ["+playerID+"]/"+action.getAction()+" 已开启限时监测");
            playerAct.put(playerID, new Object());
            ExecutorService exec = Executors.newFixedThreadPool(2);
            Future<?> future = exec.submit(() -> {
                synchronized (playerAct.get(playerID)) {
                    playerAct.get(playerID).wait();
                }
                playerAct.put(playerID, null);
                return playerID;
            });
            exec.submit(() -> {
                try {
                    future.get(time, timeUnit);
                } catch (Exception e) {
                    if (e instanceof TimeoutException) {
                        log.info("player ["+playerID+"]/"+action.getAction()+" 已超时，被系统默认处理");
                        // 根据action，做出默认操作
                        defaultAction(action, ChannelHolder.getByPlayerID(playerID));
                    } else {
                        log.error("超时检测异常", e);
                    }
                    // 不需要再次唤醒，因为做出操作后默认会调用一次checkTimeout()
                    /* synchronized(playerAct.get(playerID)){
                        playerAct.get(playerID).notify();                    
                    } */
                }
            });
        } else {
            log.info("player ["+playerID+"]/"+action.getAction()+" 已解除限时监测");
            synchronized (playerAct.get(playerID)) {
                playerAct.get(playerID).notify();
            }
        }
    }

    private static void defaultAction(ActionEnum actionEnum, Channel ch) {
        Room room=ChannelHolder.attrRoom(ch);
        Game game=room.getGame();
        Player player=ChannelHolder.attrPlayer(ch);
        if (actionEnum == ActionEnum.CALL) {
            ReqBossDTO reqBossDTO = new ReqBossDTO();
            reqBossDTO.setAction("call");
            reqBossDTO.setTendency(false);
            ch.pipeline().fireChannelRead(reqBossDTO);
        } else if (actionEnum == ActionEnum.ASK) {
            ReqBossDTO reqBossDTO = new ReqBossDTO();
            reqBossDTO.setAction("ask");
            reqBossDTO.setTendency(false);
            ch.pipeline().fireChannelRead(reqBossDTO);
        } else if (actionEnum == ActionEnum.PUT) {
            PutPokerDTO putPokerDTO = new PutPokerDTO();
            putPokerDTO.setAction("put");
            if((game.getLastPutPokers()!=null || player.getPokers().size()==0) && !game.getLastPlayerID().equals(player.getId())){
                putPokerDTO.setPutPokers(null);
                putPokerDTO.setTendency(false);
            }else{
                List<Poker> defaultPut=new ArrayList<>();
                defaultPut.add(player.getPokers().get(player.getPokers().size()-1));
                putPokerDTO.setPutPokers(defaultPut);
                putPokerDTO.setTendency(true);
            }
            ch.pipeline().fireChannelRead(putPokerDTO);
        }
    }

}
