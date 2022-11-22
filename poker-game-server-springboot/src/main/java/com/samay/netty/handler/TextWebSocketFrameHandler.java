package com.samay.netty.handler;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.samay.game.dto.MultipleDTO;
import com.samay.game.dto.PutPokerDTO;
import com.samay.game.dto.ReqBossDTO;
import com.samay.game.dto.RoomReadyDTO;
import com.samay.game.dto.TipPokerDTO;
import com.samay.game.entity.Room;
import com.samay.game.utils.RV;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import com.samay.netty.handler.holder.ChannelHolder;
import com.samay.netty.handler.http.HttpRequestHandler;
import com.samay.netty.handler.utils.WriteUtil;

@SuppressWarnings("deprecation")
@Component
@Sharable
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            // 基于http发送的ws协议请求完成连接后，即可移除处理http请求的handler，之后专门处理游戏内消息。
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
            case "call" -> {
                return ReqBossDTO.class;
            }
            case "ask" -> {
                return ReqBossDTO.class;
            }
            case "put" -> {
                return PutPokerDTO.class;
            }
            case "tip" -> {
                return TipPokerDTO.class;
            }
            case "double" -> {
                return MultipleDTO.class;
            }
            case "doublePlus" -> {
                return MultipleDTO.class;
            }
            case "noDouble" -> {
                return MultipleDTO.class;
            }
            default -> {
                return Object.class;
            }
        }
    }

    private void onJoined(ChannelHandlerContext ctx) {
        Room room = ChannelHolder.attrRoom(ctx.channel());
        ChannelGroup group = ChannelHolder.groupMap.get(ctx.channel());
        WriteUtil.writeAndFlushTextWebSocketFrame(group, RV.roomData(room));
    }

}
