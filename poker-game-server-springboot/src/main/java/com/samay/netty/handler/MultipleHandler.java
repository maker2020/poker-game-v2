package com.samay.netty.handler;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.samay.game.Game;
import com.samay.game.dto.MultipleDTO;
import com.samay.game.entity.Player;
import com.samay.game.entity.Room;
import com.samay.game.enums.ActionEnum;
import com.samay.game.enums.GameStatusEnum;
import com.samay.game.utils.TimerUtil;
import com.samay.game.vo.Notification;
import com.samay.game.vo.RV;
import com.samay.netty.handler.holder.ChannelHolder;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

@Sharable
@Component
public class MultipleHandler extends SimpleChannelInboundHandler<MultipleDTO>{

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MultipleDTO msg) throws Exception {
        ChannelGroup group=ChannelHolder.groupMap.get(ctx.channel());
        Room room=ChannelHolder.attrRoom(ctx.channel());
        Game game=room.getGame();
        Player player=ChannelHolder.attrPlayer(ctx.channel());
        
        if(game.getStatus()!=GameStatusEnum.RAISE && !player.isRaise()) return;

        Map<String,Object> resultMap=null;
        Notification notification=new Notification(player.getId());
        if(msg.isTendency()){
            notification.setChoice(true);
            if("double".equals(msg.getAction())){
                game.setMultiple(game.getMultiple()*2);
                notification.setType(ActionEnum.DOUBLE);
                resultMap=RV.multipleResultMap(notification, game.getMultiple());
            }
            if("doublePlus".equals(msg.getAction())){
                game.setMultiple(game.getMultiple()*4);
                notification.setType(ActionEnum.DOUBLE_PLUS);
                resultMap=RV.multipleResultMap(notification, game.getMultiple());
            }
        }else{
            notification.setChoice(false);
            notification.setType(ActionEnum.NO_DOUBLE);
            resultMap=RV.multipleResultMap(notification, game.getMultiple());
        }
        player.setRaise(true);
        TimerUtil.checkTimeout(ActionEnum.MULTIPLE, player.getId(), 5);
        group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(resultMap)));

        if(raiseDone(game)){
            game.setStatus(GameStatusEnum.START);

            // 缓1.5s便于玩家看清最后一名玩家是否加倍
            Thread.sleep(1000);

            // 标识加倍阶段已结束（纯方便UI逻辑）
            group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(RV.raiseStatus(true))));
            
            Player boss=null;
            for(Player p:game.getPlayers()){
                if(p.isBoss()) {
                    boss=p;
                    break;
                }
            }
            if(boss==null) throw new Exception("不可能的异常");
            // 轮到地主出牌
            Map<String,Object> putResult=RV.resultMap(ActionEnum.PUT, boss.getId(),null);
            TimerUtil.checkTimeout(ActionEnum.PUT, boss.getId(), 30);
            group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(putResult)));
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
