package com.samay.netty.handler.service;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import com.samay.game.Game;
import com.samay.game.dto.MultipleDTO;
import com.samay.game.dto.PutPokerDTO;
import com.samay.game.dto.ReqBossDTO;
import com.samay.game.entity.Player;
import com.samay.game.entity.Poker;
import com.samay.game.entity.Room;
import com.samay.game.enums.ActionEnum;
import com.samay.game.enums.GameStatusEnum;
import com.samay.game.enums.PokerTypeEnum;
import com.samay.game.enums.RoomStatusEnum;
import com.samay.game.rule.CommonRule;
import com.samay.game.utils.PokerUtil;
import com.samay.game.utils.TimerUtil;
import com.samay.netty.handler.aop.test.NotificationUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 游戏业务类
 */
@Service
@Slf4j
public class GameService {
    
    /**
     * 玩家player，准备的业务方法
     * @param player 玩家(操作者)
     * @param room 玩家所在房间
     */
    public boolean ready(Player player,Room room){
        if(player!=null && room!=null && room.getPlayers()!=null && room.getGame().getStatus()==GameStatusEnum.READY){
            for(Player p:room.getPlayers()){
                if(p.getId().equals(player.getId())){
                    p.setReady(true);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 游戏开始及初始化的业务方法: 发牌、随机开始询问开始叫地主。（其他的业务不在这里）
     * @param game
     * @param group
     * @param room
     * @param ctx
     * @throws Exception
     */
    public void gameStart(Game game,Room room) throws Exception{
        // 更新房间 (此更新操作是明确的，没有线程安全问题)
        room.setStatus(RoomStatusEnum.START);
        // 初始化游戏、准备发牌
        game = initGame(game);
        // 随机选一名玩家叫地主
        room.turnPlayer(null, ActionEnum.CALL);
        // 至此结束，其他业务由其他handler从头处理
        // ctx.fireChannelRead(Unpooled.EMPTY_BUFFER);
    }

    /**
     * 叫地主抢地主业务方法<p>
     * 注：业务方法原则上不应该包含传输层对象如channel或group等
     * @param room
     * @param player
     * @param msg
     * @return 1:成功, -1:不合法, 0:重开
     * @throws Exception
     */
    public int requestBoss(Room room, Player player, ReqBossDTO msg) throws Exception {
        if (room == null || player == null || msg == null) {
            return -1;
        }
        Game game = room.getGame();
        if (game == null || !game.getActingPlayer().equals(player.getId())
                || !game.getCurrentAction().getAction().equals(msg.getAction())) {
            return -1;
        }
        // 维护player请求序号
        // player.setReqIndex(room.getTurnCallIndex().get());
        game.getTurnCallIndex().incrementAndGet();
        player.setReqIndex(game.getTurnCallIndex().get());

        if (msg.isTendency()) {
            if (game.getCurrentAction() == ActionEnum.CALL) {
                player.callBoss();
            } else if (game.getCurrentAction() == ActionEnum.ASK) {
                player.askBoss();
                // 倍数翻一番
                game.setMultiple(game.getMultiple() * 2);
            }
            room.turnPlayer(player, ActionEnum.ASK);
        } else { // 拒绝
            if (game.getCurrentAction() == ActionEnum.CALL) {
                player.unCallBoss();
                room.turnPlayer(player, ActionEnum.CALL);
            } else if (game.getCurrentAction() == ActionEnum.ASK) {
                player.unAskBoss();
                room.turnPlayer(player, ActionEnum.ASK);
            }
        }

        // 判断是否重发
        boolean reHandout = true;
        for (Player p : game.getPlayers()) {
            if (!p.isRefuseBoss())
                reHandout = false;
        }
        if (reHandout) {
            return 0;
        }

        // 尝试获得地主
        Player boss = game.getBossInstantly();
        if (boss != null) {
            // 给地主整理新加入的牌
            boss.addAllPoker(game.getPokerBossCollector());
            PokerUtil.sort(boss.getPokers());
            // 此时可以赋值到bossPoker中对所有玩家透明了
            game.setBossPokers(game.getPokerBossCollector());

            NotificationUtil.clearPlayerNotification(room);
        }
        return 1;
    }

    /**
     * 加注阶段业务
     * @param player
     * @param room
     * @param msg
     * @return -1:不合法, 1:加注已完成, 0:加注处理成功
     * @throws Exception
     */
    public int raise(Player player,Room room,MultipleDTO msg) throws Exception{
        if(player==null || room==null || msg==null){
            return -1;
        }
        Game game=room.getGame();
        if(game==null || game.getStatus()!=GameStatusEnum.RAISE || player.isRaise() || game.getCurrentAction()!=ActionEnum.MULTIPLE){
            return -1;
        }
        if(msg.isTendency()){
            if("double".equals(msg.getAction())){
                game.setMultiple(game.getMultiple()*2);
                player.doubleMulti();
            }
            if("doublePlus".equals(msg.getAction())){
                game.setMultiple(game.getMultiple()*4);
                player.doublePlusMulti();
            }
        }else{
            player.refuseDouble();
        }
        if(raiseDone(game)){
            Player boss=game.getBossInstantly();
            game.setStatus(GameStatusEnum.START);
            game.setCurrentAction(ActionEnum.PUT);
            game.setActingPlayer(boss.getId());
            NotificationUtil.clearPlayerNotification(room);
            // 地主出牌限时
            TimerUtil.checkTimeout(ActionEnum.PUT, boss.getId());
            return 1;
        }
        return 0;
    }


    /**
     * 出牌业务方法
     * @param player
     * @param room
     * @param msg
     * @return -1:不合法, 0:出牌不合法, 1:正常出牌, 2:正常出牌且游戏结束
     * @throws Exception
     */
    public int putPoker(Player player,Room room,PutPokerDTO msg) throws Exception{
        if(player==null || room==null || msg==null){
            return -1;
        }
        Game game=room.getGame();
        if(game==null || !game.getActingPlayer().equals(player.getId()) || game.getCurrentAction()!=ActionEnum.PUT || game.getStatus()!=GameStatusEnum.START){
            return -1;
        }
        List<Poker> putPokers = msg.getPutPokers();
        Collection<Poker> lastPutPokers=game.getLastPutPokers();        
        if(game.getLastPlayerID().equals(player.getId())) lastPutPokers=null; // 清除本身压制
        // 防止恶心请求:出了牌但choice为false,于是choice参数通过实际putPoker得出，因此该参数暂时不用
        CommonRule rule = new CommonRule(putPokers, lastPutPokers);
        PokerUtil.sortForPUT(putPokers);
        if (rule.valid()) {

            player.putPokers(putPokers);
            
            if (putPokers != null) {
                game.setLastPutPokers(putPokers);
                game.setLastPlayerID(player.getId());
                
                // 炸弹翻倍
                if(rule.getPokersType()==PokerTypeEnum.BOOM){
                    game.setMultiple(game.getMultiple()*2);
                }
            }
            room.turnPlayer(player, ActionEnum.PUT);

            if (player.getPokers().size() == 0) {
                log.info("ROOM["+room.getId()+"] 游戏已结束");
                game.setStatus(GameStatusEnum.OVER);
                return 2;
            }
        }else{
            return 0;
        }
        return 1;
    }

    
    /**
     * 初始化游戏相关
     * 
     * @param group
     * @param game  (deprecated
     *              注释)不必担心jmm的工作内存无法刷新至主存。即使没有volatile，sync块内变量，在锁释放之前都会刷新到主存
     * @return
     */
    private static Game initGame(Game game) {
        // 初始化游戏数据: 发牌、更新状态
        game.restart();
        game.init();
        return game;
    }

    /**
     * 是否已经完成加注阶段
     * @return
     */
    private boolean raiseDone(Game game){
        boolean raiseDone=true;
        for(Player p:game.getPlayers()){ // CopyOnWriteArrayList
            if(!p.isRaise()) raiseDone=false;
        }
        return raiseDone;
    }

}
