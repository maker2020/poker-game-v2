package com.samay.netty.handler.game;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.samay.game.Game;
import com.samay.game.bo.Player;
import com.samay.game.bo.Room;
import com.samay.game.dto.PutPokerDTO;
import com.samay.game.enums.ActionEnum;
import com.samay.game.utils.RV;
import com.samay.game.vo.ResultVO;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import com.samay.netty.handler.holder.ChannelHolder;
import com.samay.netty.handler.service.GameService;
import com.samay.netty.handler.utils.WriteUtil;
import com.samay.service.UserService;

/**
 * <b>游戏进行过程中的出牌处理器</b>
 * <p>
 * 无太多状态变量需要关注。
 */
@Sharable
@Component
@Slf4j
public class PutPokerHandler extends SimpleChannelInboundHandler<PutPokerDTO> {

    private UserService userService;
    private GameService gameService;

    public PutPokerHandler(UserService userService,GameService gameService){
        this.userService=userService;
        this.gameService=gameService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PutPokerDTO msg) throws Exception {
        Player player = ChannelHolder.attrPlayer(ctx.channel());
        ChannelGroup group = ChannelHolder.getGroup(ctx.channel());
        Room room = ChannelHolder.attrRoom(ctx.channel());
        Game game = room.getGame();

        int res=gameService.putPoker(player, room, msg);
        if(res==-1){
            log.warn("不合规请求:PutPokerHandler->putPokerService");
            return;
        }else if(res==0){ // 反馈不合法的出牌
            // 此处仅提示操作者玩家，而非group
            Channel ch = ChannelHolder.getChannel(group, player.getId());
            ResultVO<?> result = RV.actionFail(ActionEnum.PUT);
            ch.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(result)));
        }else if(res==1){
            WriteUtil.writeAndFlushRoomDataByFilter(group);
        }else if(res==2){
            WriteUtil.writeAndFlushRoomDataByFilter(group);
            // 获取游戏结算
            Map<String,Object> gameResult=game.settlement(room);
            for(Player p:room.getPlayers()){
                userService.updatePlayer(p);                    
            }
            ResultVO<?> resultVO = RV.gameResult(gameResult);
            // fastjson禁用引用重复检测（不禁用会导致同一对象被$.ref表示，从而不便于前端解析
            group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(resultVO,SerializerFeature.DisableCircularReferenceDetect)));
            
            // 游戏重置 (点继续游戏重置，此处不用)
            game.restart();
        }
    }

}
