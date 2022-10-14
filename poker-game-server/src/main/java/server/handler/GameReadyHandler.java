package server.handler;

import game.Game;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class GameReadyHandler extends SimpleChannelInboundHandler<Game> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Game msg) throws Exception {
        System.out.println(msg);
    }
    
}
