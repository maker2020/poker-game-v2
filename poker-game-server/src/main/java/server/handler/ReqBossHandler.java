package server.handler;

import java.util.Map;

import com.alibaba.fastjson.JSON;

import game.Game;
import game.dto.ReqBossDTO;
import game.entity.Player;
import game.entity.Room;
import game.enums.ActionEnum;
import game.vo.Notification;
import game.vo.ResultVO;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import server.handler.holder.ChannelHolder;

/**
 * <b>叫地主/抢地主处理器</b><p>
 * 叫地主/抢地主轮询过程处理
 * <p>
 * 顺序不再是v1中多线程竞争，而是维护了room中的玩家list
 * <p>
 * 地主是谁？也将变得简单
 */
@Sharable
public class ReqBossHandler extends SimpleChannelInboundHandler<ReqBossDTO> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ReqBossDTO msg) throws Exception {
        Room room = ChannelHolder.attrRoom(ctx.channel());
        Player player = ChannelHolder.attrPlayer(ctx.channel());
        ChannelGroup group = ChannelHolder.groupMap.get(ctx.channel());
        Game game = room.getGame();

        // 维护player请求序号
        player.setReqIndex(room.getTurnCallIndex().get());
        room.getTurnCallIndex().incrementAndGet();

        Map<String, Object> result=null;
        if (msg.isTendency()) {
            player.reqBoss();
            if("call".equals(msg.getAction())){
                result = ResultVO.resultMap(ActionEnum.ASK, room.turnPlayer(player).getName(),
                new Notification(ActionEnum.CALL, true, player.getName()));
            }else if("ask".equals(msg.getAction())){
                result = ResultVO.resultMap(ActionEnum.ASK, room.turnPlayer(player).getName(),
                new Notification(ActionEnum.ASK, true, player.getName()));
            }
        } else { // 拒绝
            player.refuseBoss();
            if("call".equals(msg.getAction())){
                result = ResultVO.resultMap(ActionEnum.CALL, room.turnPlayer(player).getName(),
                new Notification(ActionEnum.CALL, false, player.getName()));
            }else if("ask".equals(msg.getAction())){
                result = ResultVO.resultMap(ActionEnum.ASK, room.turnPlayer(player).getName(),
                new Notification(ActionEnum.ASK, false, player.getName()));
            }
        }
        
        //
        if(result==null) throw new Exception("ReqBossDTO msg 消息异常");

        // 叫地主/抢地主最终结果：
        // 判断轮询次数是否满足最低次数
        if (room.getTurnCallIndex().get() > 2) {
            // 判断地主是否可以直接得出
            Player boss = game.getBossInstantly();
            if (boss != null) {
                Map<String, Object> result2 = ResultVO.resultMap(boss.getName());
                group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(result2)));

                // 地主既然得出，action的应更新为put，而非turn下一个玩家ask。
                // 更新result的action
                ResultVO.updateResultMap(result, ActionEnum.PUT, boss.getName());

                group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(result)));
                ctx.fireChannelRead(Unpooled.EMPTY_BUFFER);
                return;
            } else { // 自此说明需要最后一次询问
                     // do nothing
            }
        }

        group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(result)));
    }
    
}
