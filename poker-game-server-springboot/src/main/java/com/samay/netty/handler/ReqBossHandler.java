package com.samay.netty.handler;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

import com.samay.game.Game;
import com.samay.game.dto.ReqBossDTO;
import com.samay.game.entity.Player;
import com.samay.game.entity.Room;
import com.samay.game.enums.ActionEnum;
import com.samay.game.utils.PokerUtil;
import com.samay.game.vo.Notification;
import com.samay.game.vo.RV;
import com.samay.game.vo.ResultVO;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import com.samay.netty.handler.holder.ChannelHolder;

/**
 * <b>叫地主/抢地主处理器</b>
 * <p>
 * 叫地主/抢地主轮询过程处理
 * <p>
 * 顺序不再是v1中多线程竞争，而是维护了room中的玩家list
 * <p>
 * 地主是谁？也将变得简单
 */
@Sharable
@Component
@Slf4j
public class ReqBossHandler extends SimpleChannelInboundHandler<ReqBossDTO> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ReqBossDTO msg) throws Exception {
        Room room = ChannelHolder.attrRoom(ctx.channel());
        Player player = ChannelHolder.attrPlayer(ctx.channel());
        ChannelGroup group = ChannelHolder.groupMap.get(ctx.channel());
        Game game = room.getGame();

        // 针对客户端请求出牌不合规的校验
        if (!game.getActingPlayer().equals(player.getId())) {
            log.warn("player [" + player.getId() + "] 不合规请求已被拦截处理");
            return;
        }

        // 维护player请求序号
        // player.setReqIndex(room.getTurnCallIndex().get());
        game.getTurnCallIndex().incrementAndGet();
        player.setReqIndex(game.getTurnCallIndex().get());

        ResultVO<Map<String,Object>> result = null;
        if (msg.isTendency()) {
            if ("call".equals(msg.getAction())) {

                player.callBoss();

                result = RV.actionTurn(ActionEnum.ASK, room.turnPlayer(player, ActionEnum.ASK),
                        new Notification(ActionEnum.CALL, true, player.getId()));


            } else if ("ask".equals(msg.getAction())) {

                player.askBoss();

                result = RV.actionTurn(ActionEnum.ASK, room.turnPlayer(player, ActionEnum.ASK),
                        new Notification(ActionEnum.ASK, true, player.getId()));

                // 倍数翻一番
                game.setMultiple(game.getMultiple() * 2);
                ResultVO<?> multipleResult = RV.multipleInfo(game.getMultiple());
                group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(multipleResult)));
            }
        } else { // 拒绝
            if ("call".equals(msg.getAction())) {

                player.unCallBoss();

                result = RV.actionTurn(ActionEnum.CALL, room.turnPlayer(player, ActionEnum.CALL),
                        new Notification(ActionEnum.CALL, false, player.getId()));

            } else if ("ask".equals(msg.getAction())) {

                player.unAskBoss();

                result = RV.actionTurn(ActionEnum.ASK, room.turnPlayer(player, ActionEnum.ASK),
                        new Notification(ActionEnum.ASK, false, player.getId()));

            }
        }

        //
        if (result == null)
            throw new Exception("ReqBossDTO msg 消息异常");


        // 判断是否重发
        boolean reHandout = true;
        for (Player p : game.getPlayers()) {
            if (!p.isRefuseBoss())
                reHandout = false;
        }
        if (reHandout) {
            GameReadyHandler.gameStart(game, group, room, ctx);
            return;
        }
        // 尝试获得地主
        Player boss = game.getBossInstantly();
        if (boss != null) {
            ResultVO<?> bossVO = RV.boss(boss.getId());
            group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(bossVO)));

            // 广播地主牌
            ResultVO<?> bossPokersResult = RV.bossPoker(game.getPokerBossCollector());
            group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(bossPokersResult)));

            // 给地主整理新加入的牌并单独发送给地主
            boss.addAllPoker(game.getPokerBossCollector());
            PokerUtil.sort(boss.getPokers());
            ResultVO<?> resortBossPokers = RV.handoutResult(boss.getId(), boss.getPokers());
            ChannelId chID = ChannelHolder.uid_chidMap.get(boss.getId());
            group.find(chID).writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(resortBossPokers)));

            // 地主既然得出，仅通知最后一名玩家的行为即可
            // 更新result的action、turn
            RV.updateActionTurn(result, null, null);
        }

        group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(result)));

        if(boss!=null){
            // 地主已选出，此处通知进入是否加注的选择阶段
            ResultVO<?> multipleStatus=RV.raiseStatus(false);
            group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(multipleStatus)));
        }
    }

}
