package com.samay.netty.handler;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

import com.samay.game.Game;
import com.samay.game.dto.ReqBossDTO;
import com.samay.game.entity.Player;
import com.samay.game.entity.Room;
import com.samay.game.enums.ActionEnum;
import com.samay.game.utils.PokerUtil;
import com.samay.game.vo.Notification;
import com.samay.game.vo.ResultVO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import com.samay.netty.handler.holder.ChannelHolder;

/**
 * <b>叫地主/抢地主处理器</b><p>
 * 叫地主/抢地主轮询过程处理
 * <p>
 * 顺序不再是v1中多线程竞争，而是维护了room中的玩家list
 * <p>
 * 地主是谁？也将变得简单
 */
@Sharable
@Component
public class ReqBossHandler extends SimpleChannelInboundHandler<ReqBossDTO> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ReqBossDTO msg) throws Exception {
        Room room = ChannelHolder.attrRoom(ctx.channel());
        Player player = ChannelHolder.attrPlayer(ctx.channel());
        ChannelGroup group = ChannelHolder.groupMap.get(ctx.channel());
        Game game = room.getGame();

        // 针对客户端请求出牌不合规的校验
        if(!game.getActingPlayer().equals(player.getId())) return;

        // 维护player请求序号
        // player.setReqIndex(room.getTurnCallIndex().get());
        game.getTurnCallIndex().incrementAndGet();
        player.setReqIndex(game.getTurnCallIndex().get());

        Map<String, Object> result=null;
        if (msg.isTendency()) {
            player.reqBoss();
            if("call".equals(msg.getAction())){
                player.setFirstCall(true);
                result = ResultVO.resultMap(ActionEnum.ASK, room.turnPlayer(player),
                new Notification(ActionEnum.CALL, true, player.getId()));
                
            }else if("ask".equals(msg.getAction())){
                result = ResultVO.resultMap(ActionEnum.ASK, room.turnPlayer(player),
                new Notification(ActionEnum.ASK, true, player.getId()));

                // 倍数翻一番
                game.setMultiple(game.getMultiple()*2);
                Map<String,Object> multipleResult=ResultVO.mutiplying(game.getMultiple());
                group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(multipleResult)));
            }
        } else { // 拒绝
            player.refuseBoss();
            if("call".equals(msg.getAction())){
                result = ResultVO.resultMap(ActionEnum.CALL, room.turnPlayer(player),
                new Notification(ActionEnum.CALL, false, player.getId()));
            }else if("ask".equals(msg.getAction())){
                result = ResultVO.resultMap(ActionEnum.ASK, room.turnPlayer(player),
                new Notification(ActionEnum.ASK, false, player.getId()));
            }
        }
        
        //
        if(result==null) throw new Exception("ReqBossDTO msg 消息异常");

        // 叫地主/抢地主最终结果：
        // 判断轮询次数是否满足最低次数(每位玩家已做出一轮选择)
        if (game.getTurnCallIndex().get() > 2) { // 3
            // 判断是否重发
            boolean reHandout=true;
            for(Player p:game.getPlayers()){
                if(!p.isRefuseBoss()) reHandout=false;
            }
            if(reHandout){
                game.restart();
                GameReadyHandler.gameStart(game, group, room, ctx);
                return;
            }
            // 判断地主是否可以直接得出
            Player boss = game.getBossInstantly();
            if (boss != null) {
                Map<String, Object> result2 = ResultVO.resultMap(boss.getId());
                group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(result2)));

                // 广播地主牌
                Map<String,Object> bossPokersResult=ResultVO.resultMap(game.getPokerBossCollector());
                group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(bossPokersResult)));
                
                // 给地主整理新加入的牌并单独发送给地主
                boss.addAllPoker(game.getPokerBossCollector());
                PokerUtil.sort(boss.getPokers());
                Map<String,Object> resortBossPokers=ResultVO.resultMap(boss.getId(), boss.getPokers());
                ChannelId chID=ChannelHolder.uid_chidMap.get(boss.getId());
                group.find(chID).writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(resortBossPokers)));

                // 地主既然得出，action的应更新为put，而非turn下一个玩家ask。
                // 更新result的action
                ResultVO.updateResultMap(result, ActionEnum.PUT, boss.getId());

                group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(result)));
                return;
            } else { // 自此说明需要最后一次询问
                     // do nothing
            }
        }

        group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(result)));
    }
    
}
