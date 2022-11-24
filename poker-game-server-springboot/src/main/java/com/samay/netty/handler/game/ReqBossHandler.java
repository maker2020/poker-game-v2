package com.samay.netty.handler.game;

import org.springframework.stereotype.Component;

import com.samay.game.Game;
import com.samay.game.dto.ReqBossDTO;
import com.samay.game.entity.Player;
import com.samay.game.entity.Room;
import com.samay.game.enums.ActionEnum;
import com.samay.game.utils.PokerUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import lombok.extern.slf4j.Slf4j;

import com.samay.netty.handler.aop.test.NotificationUtil;
import com.samay.netty.handler.holder.ChannelHolder;
import com.samay.netty.handler.utils.WriteUtil;

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
        if (!game.getActingPlayer().equals(player.getId()) || !game.getCurrentAction().getAction().equals(msg.getAction())) {
            log.warn("player [" + player.getId() + "] 不合规请求已被拦截处理");
            return;
        }

        // 维护player请求序号
        // player.setReqIndex(room.getTurnCallIndex().get());
        game.getTurnCallIndex().incrementAndGet();
        player.setReqIndex(game.getTurnCallIndex().get());

        if (msg.isTendency()) {
            if (game.getCurrentAction()==ActionEnum.CALL) {
                player.callBoss();
            } else if (game.getCurrentAction()==ActionEnum.ASK) {
                player.askBoss();
                // 倍数翻一番
                game.setMultiple(game.getMultiple() * 2);
            }
            room.turnPlayer(player, ActionEnum.ASK);
        } else { // 拒绝
            if (game.getCurrentAction()==ActionEnum.CALL) {
                player.unCallBoss();
                room.turnPlayer(player, ActionEnum.CALL);
            } else if (game.getCurrentAction()==ActionEnum.ASK) {
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
            GameReadyHandler.gameStart(game, group, room, ctx);
            return;
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

        WriteUtil.writeAndFlushRoomDataByFilter(group);
    }

}
