package server.handler;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import game.dto.RoomReadyDTO;
import game.entity.Player;
import game.entity.Room;
import game.enums.RoomStatusEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import server.handler.holder.RoomHolder;

/**
 * RoomReadyHandler<p>
 * 主要用来处理玩家发起的准备消息
 */
@Sharable
public class RoomReadyHandler extends SimpleChannelInboundHandler<RoomReadyDTO>{

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RoomReadyDTO dto) throws Exception {
        if(!RoomStatusEnum.READY.equals(RoomStatusEnum.valueOf(dto.getAction().toUpperCase()))){
            return;
        }
        if(dto.getTendency().equalsIgnoreCase("Y")){
            Player player=(Player) (ctx.channel().attr(AttributeKey.valueOf("player")).get());
            ChannelGroup group=RoomHolder.playerChannelGroup.get(player.getName());
            player.setReady(true);
            Map<String,Object> msg=new HashMap<>();
            msg.put("user", player.getName());
            msg.put("ready", true);
            // 设计问题记录：先准备的玩家channel陷入阻塞，将无法read消息，造成延迟显示数据
            // 解决：不再用并发包的阻塞，始终不阻塞(放行)，通过count人数来放行
            // 推荐另一种：业务层面思考，将发牌逻辑放到GameReady中处理
            group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(msg)));
            
            // 进入下一环节：游戏准备阶段(初始化NormalGame)
            Room room=(Room)(ctx.channel().attr(AttributeKey.valueOf("room")).get());
            ctx.fireChannelRead(room.getGame());
        }

    }
    
}
