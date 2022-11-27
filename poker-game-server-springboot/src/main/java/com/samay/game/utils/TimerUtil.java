package com.samay.game.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.samay.game.Game;
import com.samay.game.dto.MultipleDTO;
import com.samay.game.dto.PutPokerDTO;
import com.samay.game.dto.ReqBossDTO;
import com.samay.game.entity.Player;
import com.samay.game.entity.Poker;
import com.samay.game.entity.Room;
import com.samay.game.enums.ActionEnum;
import com.samay.netty.handler.holder.ChannelHolder;
import com.samay.netty.handler.holder.RoomManager;
import com.samay.netty.handler.service.GameService;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 * <b>时间管理相关工具</b>
 * <p>
 * 例: 限时xx秒完成操作，但注意：由于包含了超时默认操作，因此依赖了GameService业务对象
 */
@Slf4j
@Component
public class TimerUtil {

    private static final TimeUnit timeUnit = TimeUnit.SECONDS;

    private static Map<String, Object> playerAct = new ConcurrentHashMap<>();
    private static final Object NULL = new Object();

    private static GameService gameService;

    @Autowired
    public TimerUtil(GameService gameService){
        TimerUtil.gameService=gameService;
    }

    /**
     * <b>指定操作类型的时间监测</b>
     * <p>
     * 即在一定的时间内没有检测到玩家发起的相关action请求，系统将按照默认行为方式处理
     * 
     * @param time   时限(单位: 秒)
     * @param action 什么操作
     * @param ch     通道
     */
    public static void checkTimeout(ActionEnum action, String playerID) throws Exception {
        long time = switch (action) {
            case ASK -> 10;
            case CALL -> 10;
            case MULTIPLE -> 5;
            case PUT -> 25;
            default -> throw new Exception("错误的限时器参数");
        };
        synchronized (playerID) {
            if (playerID == null || action == null)
                return;
            if (playerAct.get(playerID) == null || playerAct.get(playerID) == NULL) {
                log.info("player [" + playerID + "]/" + action.getAction() + " 已开启限时监测");
                playerAct.put(playerID, new Object());
                ExecutorService exec = Executors.newFixedThreadPool(2);
                Future<?> future = exec.submit(() -> {
                    synchronized (playerAct.get(playerID)) {
                        playerAct.get(playerID).wait();
                    }
                    playerAct.put(playerID, NULL);
                    return playerID;
                });
                exec.submit(() -> {
                    try {
                        future.get(time, timeUnit);
                    } catch (Exception e) {
                        if (e instanceof TimeoutException) {
                            log.info("player [" + playerID + "]/" + action.getAction() + " 已超时，被系统默认处理");
                            // 根据action，做出默认操作
                            try {
                                defaultAction(action, playerID);
                            } catch (Exception e1) {
                                log.error("玩家默认操作异常：玩家或房间不存在", e1);
                                // 刷新锁资源
                            }
                        } else {
                            log.error("超时检测异常", e);
                        }
                        // 不需要再次唤醒，因为做出操作后默认会调用一次checkTimeout()
                        /*
                         * synchronized(playerAct.get(playerID)){
                         * playerAct.get(playerID).notify();
                         * }
                         */
                    }
                });
            } else {
                if (playerAct.get(playerID) == null)
                    return;
                log.info("player [" + playerID + "]/" + action.getAction() + " 已解除限时监测");
                synchronized (playerAct.get(playerID)) {
                    playerAct.get(playerID).notify();
                }
            }
        }
    }

    private static void defaultAction(ActionEnum actionEnum, String playerID) throws Exception {
        Channel ch=ChannelHolder.getChannel(playerID);
        if(ch==null){
            defaultActionOffline(actionEnum,playerID);
            return;
        }
        Room room = ChannelHolder.attrRoom(ch);
        Game game = room.getGame();
        Player player = ChannelHolder.attrPlayer(ch);
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
            if ((game.getLastPutPokers() != null || player.getPokers().size() == 0)
                    && !game.getLastPlayerID().equals(player.getId())) {
                putPokerDTO.setPutPokers(null);
                putPokerDTO.setTendency(false);
            } else {
                List<Poker> defaultPut = new ArrayList<>();
                defaultPut.add(player.getPokers().get(player.getPokers().size() - 1));
                putPokerDTO.setPutPokers(defaultPut);
                putPokerDTO.setTendency(true);
            }
            ch.pipeline().fireChannelRead(putPokerDTO);
        } else if (actionEnum == ActionEnum.MULTIPLE) {
            MultipleDTO multipleDTO = new MultipleDTO();
            multipleDTO.setAction("unDouble");
            multipleDTO.setTendency(false);
            ch.pipeline().fireChannelRead(multipleDTO);
        }
    }

    private static void defaultActionOffline(ActionEnum actionEnum,String playerID) throws Exception{
        Game game=null;
        Player player=null;
        Room room=null;
        Set<Room> roomSet=RoomManager.getAllRooms();
        Iterator<Room> it=roomSet.iterator();
        done:while (it.hasNext()) {
            Room r=it.next();
            List<Player> players=r.getPlayers();
            for(Player p:players){
                if(p.getId().equals(playerID)){
                    game=r.getGame();
                    player=p;
                    room=r;
                    break done;
                }
            }
        }
        if(game==null || player==null || room==null) throw new Exception("超时默认操作异常：玩家或游戏不存在");
        if (actionEnum == ActionEnum.CALL) {
            ReqBossDTO reqBossDTO = new ReqBossDTO();
            reqBossDTO.setAction("call");
            reqBossDTO.setTendency(false);
            gameService.requestBoss(room, player, reqBossDTO);
        } else if (actionEnum == ActionEnum.ASK) {
            ReqBossDTO reqBossDTO = new ReqBossDTO();
            reqBossDTO.setAction("ask");
            reqBossDTO.setTendency(false);
            gameService.requestBoss(room, player, reqBossDTO);
        } else if (actionEnum == ActionEnum.PUT) {
            PutPokerDTO putPokerDTO = new PutPokerDTO();
            putPokerDTO.setAction("put");
            if ((game.getLastPutPokers() != null || player.getPokers().size() == 0)
                    && !game.getLastPlayerID().equals(player.getId())) {
                putPokerDTO.setPutPokers(null);
                putPokerDTO.setTendency(false);
            } else {
                List<Poker> defaultPut = new ArrayList<>();
                defaultPut.add(player.getPokers().get(player.getPokers().size() - 1));
                putPokerDTO.setPutPokers(defaultPut);
                putPokerDTO.setTendency(true);
            }
            gameService.putPoker(player, room, putPokerDTO);
        } else if (actionEnum == ActionEnum.MULTIPLE) {
            MultipleDTO multipleDTO = new MultipleDTO();
            multipleDTO.setAction("unDouble");
            multipleDTO.setTendency(false);
            gameService.raise(player, room, multipleDTO);
        }
    }

}
