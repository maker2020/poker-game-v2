package com.samay.netty.handler.game;

import org.springframework.stereotype.Component;

import com.samay.game.bo.Player;
import com.samay.game.bo.Room;
import com.samay.game.dto.MultipleDTO;
import com.samay.netty.handler.holder.ChannelHolder;
import com.samay.netty.handler.service.GameService;
import com.samay.netty.handler.utils.WriteUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import lombok.extern.slf4j.Slf4j;

@Sharable
@Component
@Slf4j
public class MultipleHandler extends SimpleChannelInboundHandler<MultipleDTO>{

    private GameService gameService;

    public MultipleHandler(GameService gameService){
        this.gameService=gameService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MultipleDTO msg) throws Exception {
        ChannelGroup group=ChannelHolder.getGroup(ctx.channel());
        Room room=ChannelHolder.attrRoom(ctx.channel());
        Player player=ChannelHolder.attrPlayer(ctx.channel());
        
        int res=gameService.raise(player, room, msg);
        if(res==-1){
            log.warn("不合规请求:MultipleHandler->raiseService");
            return;
        }else{
            WriteUtil.writeAndFlushRoomDataByFilter(group);
        }
    }
    
}
