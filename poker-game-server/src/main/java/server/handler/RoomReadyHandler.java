package server.handler;

import game.entity.Room;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RoomReadyHandler extends SimpleChannelInboundHandler<Room>{

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Room room) throws Exception {
                  
    }
    
}
