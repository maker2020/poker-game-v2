package server.handler;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;

import game.dto.PutPokerDTO;
import game.dto.ReqBossDTO;
import game.dto.RoomReadyDTO;
import game.entity.Player;
import game.entity.Room;
import game.vo.ResultVO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import server.handler.holder.ChannelHolder;
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
            case "call" -> {
                return ReqBossDTO.class;
            }
            case "ask" -> {
                return ReqBossDTO.class;
            }
            case "put" -> {
                return PutPokerDTO.class;
            }
            default -> {
                return Object.class;
            }
        }
    }

    private void onJoined(ChannelHandlerContext ctx) {
        Player player = ChannelHolder.attrPlayer(ctx.channel());
        Room room = ChannelHolder.attrRoom(ctx.channel());

        List<Player> playerList=room.getPlayers();
        String[] playerNameArr=new String[playerList.size()];
        for(int i=0;i<playerList.size();i++){
            playerNameArr[i]=playerList.get(i).getName();
        }

        Map<String,Object> msg=ResultVO.resultMap(player.getName(), room.getId(), playerNameArr);
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(JSON.toJSONString(msg));
        ChannelGroup group = ChannelHolder.groupMap.get(ctx.channel());
        group.writeAndFlush(textWebSocketFrame);
    }

}
