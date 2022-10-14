package server.handler;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;

import game.dto.GameStartDTO;
import game.dto.RoomReadyDTO;
import game.entity.Player;
import game.entity.Room;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.AttributeKey;
import server.handler.holder.RoomHolder;
import server.handler.http.HttpRequestHandler;

@SuppressWarnings("deprecation")
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            ctx.pipeline().remove(HttpRequestHandler.class);
            // 完成握手后的获取用户、房间相关信息
            onJoined(ctx);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String message = msg.text();
        if (!JSONValidator.from(message).validate())
            return;
        // 将message(json数据)转化为java对象，从而由不同Handler<Class>处理
        Class<?> clazz = dispatcher(message);
        Object object = JSON.parseObject(message, clazz);
        // 触发下一个handler（游戏准备阶段业务）
        ctx.fireChannelRead(object);// 注：retain()、否则netty4中ctx默认调用release()回收导致异常
    }

    /**
     * 根据message决定消息类型所对应的Java类型，并返回
     * 
     * @param message
     * @return
     */
    private Class<?> dispatcher(String message) {
        JSONObject obj = JSON.parseObject(message);
        String action = obj.getString("action");
        switch (action) {
            case "ready" -> {
                return RoomReadyDTO.class;
            }
            case "start" -> {
                return GameStartDTO.class;
            }
            default -> {
                return Object.class;
            }
        }
    }

    private void onJoined(ChannelHandlerContext ctx) {
        // result
        Map<String, Object> msg = new HashMap<>();
        Player player = (Player) (ctx.channel().attr(AttributeKey.valueOf("player")).get());
        Room room = (Room) (ctx.channel().attr(AttributeKey.valueOf("room")).get());
        msg.put("user", player.getName());
        msg.put("roomID", room.getId());
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(JSON.toJSONString(msg));
        ChannelGroup group = RoomHolder.playerChannelGroup.get(player.getName());
        group.writeAndFlush(textWebSocketFrame);
    }

}
