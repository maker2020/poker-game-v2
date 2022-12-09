package com.samay.netty.handler.game;

import org.springframework.stereotype.Component;

import com.samay.game.bo.Player;
import com.samay.game.bo.Room;
import com.samay.game.dto.RoomReadyDTO;
import com.samay.game.utils.RV;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import com.samay.netty.handler.holder.ChannelHolder;
import com.samay.netty.handler.service.GameService;
import com.samay.netty.handler.utils.WriteUtil;

/**
 * RoomReadyHandler<p>
 * 主要用来处理玩家发起的准备消息
 */
@Sharable
@Component
public class RoomReadyHandler extends SimpleChannelInboundHandler<RoomReadyDTO>{

    private GameService gameService;

    public RoomReadyHandler(GameService gameService){
        this.gameService=gameService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RoomReadyDTO dto) throws Exception {
        if(dto.isTendency()){
            Player player=ChannelHolder.attrPlayer(ctx.channel());
            ChannelGroup group=ChannelHolder.getGroup(ctx.channel());
            Room room=ChannelHolder.attrRoom(ctx.channel());
            if(gameService.ready(player, room)){
                WriteUtil.writeAndFlushTextWebSocketFrame(group, RV.roomData(room));
                ctx.fireChannelRead(room.getGame());
            }
        }
    }
    

                // 设计问题记录：先准备的玩家channel陷入阻塞，将无法read消息，造成延迟显示数据
            // 解决：不再用并发包的阻塞，始终不阻塞(放行)，通过count人数来放行
            // 推荐另一种：业务层面思考，将发牌逻辑放到GameReady中处理
            
            // 进入下一环节：游戏准备阶段(初始化NormalGame)
}
