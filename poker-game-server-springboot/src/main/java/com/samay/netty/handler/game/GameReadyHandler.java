package com.samay.netty.handler.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.samay.game.Game;
import com.samay.game.entity.Room;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import lombok.extern.slf4j.Slf4j;
import com.samay.netty.handler.holder.ChannelHolder;
import com.samay.netty.handler.service.GameService;
import com.samay.netty.handler.utils.WriteUtil;

/**
 * 游戏准备阶段:
 * <p>
 * 验证人数是否
 */
@Sharable
@Component
@Slf4j
public class GameReadyHandler extends SimpleChannelInboundHandler<Game> {

    private GameService gameService;

    @Autowired
    public GameReadyHandler(GameService gameService){
        this.gameService=gameService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Game game) throws Exception {
        if (!canStart(game))
            return;
        // 游戏发牌阶段: 仅最后一个进入的线程处理发牌工作
        ChannelGroup group = ChannelHolder.getGroup(ctx.channel());
        Room room = ChannelHolder.attrRoom(ctx.channel());
        log.info("RoomID[" + room.getId() + "]:ready to start");

        gameService.gameStart(game, room);
        WriteUtil.writeAndFlushRoomDataByFilter(group);
    }

    /**
     * 任意玩家没准备，返回false
     * <p>
     * game.players是安全容器，这里不用关注线程安全
     * 
     * @param game
     * @return
     */
    private boolean canStart(Game game) {
        boolean can = true;
        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (!game.getPlayers().get(i).isReady() || game.getPlayers().size() < 3)
                can = false;
        }
        return can;
    }


    /**
     * 断连日志
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("disconnected: " + ctx.channel().remoteAddress());
    }

}


// 发牌至玩家(只有一个线程处理，所以需要分发group里的channel)
        /* // 注：这里嵌套循环数量级不大，并不影响性能，没必要在game实现类中建立维护hash结构存储变量提升性能。
        Iterator<Channel> it = group.iterator();
        while (it.hasNext()) {
            Channel ch = it.next();
            Player p = ChannelHolder.attrPlayer(ch);
            for (int i = 0; i < game.getPlayers().size(); i++) {
                if (p.getId().equals(game.getPlayers().get(i).getId())) {
                    ResultVO<?> resultVO = RV.handoutResult(p.getId(), p.getPokers());
                    ch.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(resultVO)));
                    break;
                }
            }
        } */