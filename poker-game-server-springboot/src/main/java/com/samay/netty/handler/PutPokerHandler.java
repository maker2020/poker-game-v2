package com.samay.netty.handler;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.samay.game.Game;
import com.samay.game.dto.PutPokerDTO;
import com.samay.game.entity.Player;
import com.samay.game.entity.Poker;
import com.samay.game.entity.Room;
import com.samay.game.enums.ActionEnum;
import com.samay.game.enums.PokerTypeEnum;
import com.samay.game.rule.CommonRule;
import com.samay.game.utils.PokerUtil;
import com.samay.game.vo.Notification;
import com.samay.game.vo.ResultVO;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import com.samay.netty.handler.holder.ChannelHolder;

/**
 * <b>游戏进行过程中的出牌处理器</b>
 * <p>
 * 无太多状态变量需要关注。
 */
@Sharable
@Component
public class PutPokerHandler extends SimpleChannelInboundHandler<PutPokerDTO> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PutPokerDTO msg) throws Exception {
        Player player = ChannelHolder.attrPlayer(ctx.channel());
        ChannelGroup group = ChannelHolder.groupMap.get(ctx.channel());
        Room room = ChannelHolder.attrRoom(ctx.channel());
        Game game = room.getGame();
        // 针对客户端请求出牌不合规的校验
        if(!game.getActingPlayer().equals(player.getId())) return;

        List<Poker> putPokers = msg.getPutPokers();
        Collection<Poker> lastPutPokers=game.getLastPutPokers();        
        if(game.getLastPlayerID().equals(player.getId())) lastPutPokers=null; // 清除本身压制

        // 防止恶心请求:出了牌但choice为false,于是choice参数通过实际putPoker得出，因此该参数暂时不用
        CommonRule rule = new CommonRule(putPokers, lastPutPokers);
        PokerUtil.sortForPUT(putPokers);
        if (rule.valid()) {
            Map<String, Object> result = ResultVO.resultMap(ActionEnum.PUT, room.turnPlayer(player),
                    new Notification(ActionEnum.PUT, putPokers != null, player.getId()), putPokers,
                    putPokers == null ? player.getPokers().size() : player.getPokers().size() - putPokers.size());
            group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(result)));

            if (putPokers != null) {
                player.removeAllPoker(putPokers);
                game.setLastPutPokers(putPokers);
                game.setLastPlayerID(player.getId());
                
                // 炸弹翻倍
                if(rule.getPokersType()==PokerTypeEnum.BOOM){
                    game.setMultiple(game.getMultiple()*2);
                    Map<String,Object> multiple=ResultVO.mutiplying(game.getMultiple());
                    group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(multiple)));
                }

                if (player.getPokers().size() == 0) {
                    // 游戏结算
                    Map<String,Object> gameResult=game.settlement();
                    Map<String, Object> resultVO = ResultVO.gameResult(gameResult);
                    // fastjson禁用引用重复检测（不禁用会导致同一对象被$.ref表示，从而不便于前端解析
                    group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(resultVO,SerializerFeature.DisableCircularReferenceDetect)));
                }
            }
        } else { // 反馈不合法
                 // 此处仅提示操作者玩家，而非group
            Channel ch = group.find(ChannelHolder.uid_chidMap.get(player.getId()));
            Map<String, Object> result = ResultVO.actionFail(ActionEnum.PUT);
            ch.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(result)));
        }
    }

}
