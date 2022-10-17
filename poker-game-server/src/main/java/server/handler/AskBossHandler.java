package server.handler;

import java.util.Map;

import com.alibaba.fastjson.JSON;

import game.dto.AskBossDTO;
import game.entity.Player;
import game.entity.Room;
import game.enums.ActionEnum;
import game.vo.Notification;
import game.vo.ResultVO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import server.handler.holder.ChannelHolder;

/**
 * 抢地主处理
 */
public class AskBossHandler extends SimpleChannelInboundHandler<AskBossDTO>{

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AskBossDTO msg) throws Exception {
        Room room = ChannelHolder.attrRoom(ctx.channel());
        Player player = ChannelHolder.attrPlayer(ctx.channel());
        ChannelGroup group = ChannelHolder.groupMap.get(ctx.channel());
        
        player.setReqIndex(room.getTurnCallIndex().get());
        if(msg.isTendency()){
            player.reqBoss();
            Map<String,Object> result=ResultVO.resultMap(ActionEnum.ASK, room.turnPlayer(player).getName(), new Notification(ActionEnum.ASK, true, player.getName()));
            group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(result)));
        }else{
            player.refuseBoss();
            Map<String,Object> result=ResultVO.resultMap(ActionEnum.ASK, room.turnPlayer(player).getName(), new Notification(ActionEnum.ASK, false, player.getName()));
            group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(result)));
        }
    }
    
}
