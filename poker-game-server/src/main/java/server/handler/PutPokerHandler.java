package server.handler;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import game.dto.PutPokerDTO;
import game.entity.Player;
import game.entity.Poker;
import game.entity.Room;
import game.enums.ActionEnum;
import game.vo.Notification;
import game.vo.ResultVO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import server.handler.holder.ChannelHolder;

/**
 * <b>游戏进行过程中的出牌处理器</b><p>
 * 无太多状态变量需要关注。
 */
@Sharable
public class PutPokerHandler extends SimpleChannelInboundHandler<PutPokerDTO>{

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PutPokerDTO msg) throws Exception {
        Player player=ChannelHolder.attrPlayer(ctx.channel());
        ChannelGroup group=ChannelHolder.groupMap.get(ctx.channel());
        Room room=ChannelHolder.attrRoom(ctx.channel());
        List<Poker> putPokers=msg.getPutPokers();
        boolean choice=msg.isTendency();
        
        Map<String,Object> result=ResultVO.resultMap(ActionEnum.PUT, room.turnPlayer(player).getName(), new Notification(ActionEnum.PUT,choice,player.getName()), putPokers);
        group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(result)));
    }
    
}
