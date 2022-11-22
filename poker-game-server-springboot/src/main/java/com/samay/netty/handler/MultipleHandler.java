package com.samay.netty.handler;

import org.springframework.stereotype.Component;

import com.samay.game.Game;
import com.samay.game.dto.MultipleDTO;
import com.samay.game.entity.Player;
import com.samay.game.entity.Room;
import com.samay.game.enums.ActionEnum;
import com.samay.game.enums.GameStatusEnum;
import com.samay.game.utils.TimerUtil;
import com.samay.netty.handler.aop.test.NotificationUtil;
import com.samay.netty.handler.holder.ChannelHolder;
import com.samay.netty.handler.utils.WriteUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;

@Sharable
@Component
public class MultipleHandler extends SimpleChannelInboundHandler<MultipleDTO>{

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MultipleDTO msg) throws Exception {
        ChannelGroup group=ChannelHolder.groupMap.get(ctx.channel());
        Room room=ChannelHolder.attrRoom(ctx.channel());
        Game game=room.getGame();
        Player player=ChannelHolder.attrPlayer(ctx.channel());
        
        if(game.getStatus()!=GameStatusEnum.RAISE || player.isRaise() || game.getCurrentAction()!=ActionEnum.MULTIPLE) return;

        if(msg.isTendency()){
            if("double".equals(msg.getAction())){
                game.setMultiple(game.getMultiple()*2);
                player.doubleMulti();
            }
            if("doublePlus".equals(msg.getAction())){
                game.setMultiple(game.getMultiple()*4);
                player.doublePlusMulti();
            }
        }else{
            player.refuseDouble();
        }

        WriteUtil.writeAndFlushRoomDataByFilter(group);

        if(raiseDone(game)){
            Player boss=game.getBossInstantly();
            game.setStatus(GameStatusEnum.START);
            game.setCurrentAction(ActionEnum.PUT);
            game.setActingPlayer(boss.getId());

            // 缓1.5s便于玩家看清最后一名玩家是否加倍
            Thread.sleep(1000);
            NotificationUtil.clearPlayerNotification(room);

            WriteUtil.writeAndFlushRoomDataByFilter(group);
        
            // 地主出牌限时
            TimerUtil.checkTimeout(ActionEnum.PUT, boss.getId(), 30);
        }

    }

    /**
     * 是否已经完成加注阶段
     * @return
     */
    private boolean raiseDone(Game game){
        boolean raiseDone=true;
        for(Player p:game.getPlayers()){ // CopyOnWriteArrayList
            if(!p.isRaise()) raiseDone=false;
        }
        return raiseDone;
    }
    
}
