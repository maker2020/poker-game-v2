package com.samay.netty.handler;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.samay.game.Game;
import com.samay.game.dto.TipPokerDTO;
import com.samay.game.entity.Player;
import com.samay.game.entity.Poker;
import com.samay.game.entity.Room;
import com.samay.game.utils.PokerUtil;
import com.samay.game.vo.RV;
import com.samay.netty.handler.holder.ChannelHolder;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

@Sharable
@Component
public class TipPokerHandler extends SimpleChannelInboundHandler<TipPokerDTO> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TipPokerDTO tipDTO) throws Exception {
        Channel ch=ctx.channel();
        Room room=ChannelHolder.attrRoom(ch);
        Game game=room.getGame();
        Player player=ChannelHolder.attrPlayer(ch);
        if(!player.getId().equals(game.getActingPlayer())) return;
        if(game.getLastPutPokers()==null) return;
        List<List<Poker>> resultList=PokerUtil.tipPokers(player,game);
        Map<String,Object> result;
        if(resultList==null || resultList.size()==0){
            // 返回没有提示
            result=RV.tipResult(null, false);
        }else{
            // 根据请求次数轮流选取resultList中的牌型组
            result=RV.tipResult(resultList.get(tipDTO.getTipIndex()%resultList.size()), true);
        }
        ch.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(result)));
    }
    
}
