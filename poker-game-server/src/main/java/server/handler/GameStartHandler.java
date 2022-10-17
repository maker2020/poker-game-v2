package server.handler;

import game.dto.GameStartDTO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class GameStartHandler extends SimpleChannelInboundHandler<GameStartDTO>{

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GameStartDTO msg) throws Exception {
        
    }

}
