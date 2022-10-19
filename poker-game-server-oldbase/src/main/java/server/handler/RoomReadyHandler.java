package server.handler;

import java.util.Map;

import com.alibaba.fastjson.JSON;

import game.dto.RoomReadyDTO;
import game.entity.Player;
import game.entity.Room;
import game.vo.ResultVO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import server.handler.holder.ChannelHolder;

/**
 * RoomReadyHandler<p>
 * 主要用来处理玩家发起的准备消息
 */
@Sharable
public class RoomReadyHandler extends SimpleChannelInboundHandler<RoomReadyDTO>{

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RoomReadyDTO dto) throws Exception {
        if(dto.isTendency()){
            Player player=ChannelHolder.attrPlayer(ctx.channel());
            ChannelGroup group=ChannelHolder.groupMap.get(ctx.channel());
            player.setReady(true);
            Map<String,Object> msg=ResultVO.resultMap(player.getName(), true);
            // 设计问题记录：先准备的玩家channel陷入阻塞，将无法read消息，造成延迟显示数据
            // 解决：不再用并发包的阻塞，始终不阻塞(放行)，通过count人数来放行
            // 推荐另一种：业务层面思考，将发牌逻辑放到GameReady中处理
            group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(msg)));
            
            // 进入下一环节：游戏准备阶段(初始化NormalGame)
            Room room=ChannelHolder.attrRoom(ctx.channel());
            ctx.fireChannelRead(room.getGame());
        }
    }
    
}
