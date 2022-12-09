package com.samay.netty.handler.game;

import org.springframework.stereotype.Component;

import com.samay.game.bo.Player;
import com.samay.game.bo.Room;
import com.samay.game.dto.ReqBossDTO;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import lombok.extern.slf4j.Slf4j;

import com.samay.netty.handler.holder.ChannelHolder;
import com.samay.netty.handler.service.GameService;
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

    private GameService gameService;

    public ReqBossHandler(GameService gameService){
        this.gameService=gameService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ReqBossDTO msg) throws Exception {
        Room room = ChannelHolder.attrRoom(ctx.channel());
        Player player = ChannelHolder.attrPlayer(ctx.channel());
        ChannelGroup group = ChannelHolder.getGroup(ctx.channel());

        int res = gameService.requestBoss(room, player, msg);
        if (res == 0) {
            gameService.gameStart(room.getGame(), room);
            WriteUtil.writeAndFlushRoomDataByFilter(group);
        } else if (res == 1) {
            // 将处理完的业务对象传给客户端
            WriteUtil.writeAndFlushRoomDataByFilter(group);
        } else if (res == -1) {
            log.warn("不合规请求:ReqBossHandler->requestBossService");
        }
    }

}
