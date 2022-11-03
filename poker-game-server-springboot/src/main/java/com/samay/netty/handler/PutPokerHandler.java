package com.samay.netty.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

import com.samay.game.dto.PutPokerDTO;
import com.samay.game.entity.Player;
import com.samay.game.entity.Poker;
import com.samay.game.entity.Room;
import com.samay.game.enums.ActionEnum;
import com.samay.game.vo.Notification;
import com.samay.game.vo.ResultVO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import com.samay.netty.handler.holder.ChannelHolder;

/**
 * <b>游戏进行过程中的出牌处理器</b><p>
 * 无太多状态变量需要关注。
 */
@Sharable
@Component
public class PutPokerHandler extends SimpleChannelInboundHandler<PutPokerDTO>{

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PutPokerDTO msg) throws Exception {
        Player player=ChannelHolder.attrPlayer(ctx.channel());
        ChannelGroup group=ChannelHolder.groupMap.get(ctx.channel());
        Room room=ChannelHolder.attrRoom(ctx.channel());
        List<Poker> putPokers=msg.getPutPokers();
        boolean choice=msg.isTendency();
        
        Map<String,Object> result=ResultVO.resultMap(ActionEnum.PUT, room.turnPlayer(player), new Notification(ActionEnum.PUT,choice,player.getName()), putPokers, putPokers==null?player.getPokers().size():player.getPokers().size()-putPokers.size());
        group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(result)));
        if(putPokers!=null) {
            player.removeAllPoker(putPokers);
            if(player.getPokers().size()==0){
                List<String> winnerIdList=new ArrayList<>();
                if(player.isBoss()){
                    winnerIdList.add(player.getName());
                }else{
                    for(Player p:room.getPlayers()){
                        if(!p.isBoss()){
                            winnerIdList.add(p.getName());
                        }
                    }
                }
                Map<String,Object> gameResult=ResultVO.gameResult(winnerIdList);
                group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(gameResult)));
            }
        }
    }
    
}
