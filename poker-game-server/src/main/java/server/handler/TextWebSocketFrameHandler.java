package server.handler;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import game.entity.Player;
import game.entity.Room;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.AttributeKey;
import server.handler.holder.RoomHolder;

@SuppressWarnings("deprecation")
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame>{

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx,Object evt) throws Exception{
        if(evt==WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE){
            ctx.pipeline().remove(HttpRequestHandler.class);
            // 完成握手后的获取用户、房间相关信息
            onJoined(ctx);
        }else{
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {        
        // 获取player对应的ChannelGroup
        Player player = (Player) (ctx.channel().attr(AttributeKey.valueOf("player")).get()); 
        ChannelGroup group=RoomHolder.playerChannelGroup.get(player.getName());
        group.writeAndFlush(msg.retain());
    }

    private void onJoined(ChannelHandlerContext ctx){
        // result
        Map<String, Object> msg = new HashMap<>();
        Player player = (Player) (ctx.channel().attr(AttributeKey.valueOf("player")).get()); 
        Room room = (Room) (ctx.channel().attr(AttributeKey.valueOf("room")).get());
        msg.put("player", player);
        msg.put("room", room);
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(JSON.toJSONString(msg));
        ChannelGroup group=RoomHolder.playerChannelGroup.get(player.getName());
        group.writeAndFlush(textWebSocketFrame);
    }
    
}
