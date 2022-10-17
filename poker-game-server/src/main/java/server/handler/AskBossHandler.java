package server.handler;

import java.util.Map;

import com.alibaba.fastjson.JSON;

import game.Game;
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
        Game game=room.getGame();

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

        // 叫地主/抢地主最终结果：
        // 判断轮询次数是否满足最低次数
        if(room.getTurnCallIndex().get()>2){
            // 判断地主是否可以直接得出
            Player boss=game.getBossInstantly();
            if(boss!=null){
                Map<String,Object> result=ResultVO.resultMap(boss.getName());
                group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(result)));
            }else{ // 自此说明需要最后一次询问
                // do nothing
            }
        }
    }
    
}
