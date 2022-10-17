package server.handler;

import game.dto.AskBossDTO;
import game.entity.Player;
import game.entity.Room;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import server.handler.holder.ChannelHolder;

/**
 * 抢地主处理
 */
public class AskBossHandler extends SimpleChannelInboundHandler<AskBossDTO>{

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AskBossDTO msg) throws Exception {
        Room room = ChannelHolder.attrRoom(ctx.channel());
        Player player = ChannelHolder.attrPlayer(ctx.channel());
        ChannelGroup group = ChannelHolder.groupMap.get(ctx.channel());
        
    }
    
}
