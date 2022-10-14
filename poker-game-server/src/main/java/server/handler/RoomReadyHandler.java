package server.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import com.alibaba.fastjson.JSON;

import game.Game;
import game.NormalGame;
import game.dto.RoomReadyDTO;
import game.entity.Player;
import game.entity.Room;
import game.enums.RoomStatusEnum;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import server.handler.holder.RoomHolder;

/**
 * 共享实例的RoomReadyHandler<p>
 * 主要用来处理玩家准备消息
 */
@Sharable
@Slf4j
public class RoomReadyHandler extends SimpleChannelInboundHandler<RoomReadyDTO>{

    private CountDownLatch readyLatch=new CountDownLatch(3);
    private volatile Game game;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RoomReadyDTO dto) throws Exception {
        if(!RoomStatusEnum.READY.equals(RoomStatusEnum.valueOf(dto.getAction().toUpperCase()))){
            return;
        }
        if(dto.getTendency().equalsIgnoreCase("Y")){
            Player player=(Player) (ctx.channel().attr(AttributeKey.valueOf("player")).get());
            ChannelGroup group=RoomHolder.playerChannelGroup.get(player.getName());
            player.setReady(true);
            Map<String,Object> msg=new HashMap<>();
            msg.put("user", player.getName());
            msg.put("ready", true);
            // bug记录：先准备的玩家channel陷入阻塞，将无法read消息，造成延迟显示数据
            group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(msg)));
            //
            readyLatch.countDown();
            readyLatch.await();
            Room room=(Room)(ctx.channel().attr(AttributeKey.valueOf("room")).get());
            log.info("RoomID["+room.getId()+"]:ready to start");
            // 初始化游戏、准备发牌    
            game=initGame(group);
            // 发牌
            for(int i=0;i<game.getPlayers().size();i++){
                Player p=game.getPlayers().get(i);
                if(p.getName().equals(player.getName())){
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(p.getPokers())));
                }
            }
            // 进入下一环节：游戏准备阶段
            ctx.fireChannelRead(game);
        }
    }

    private synchronized Game initGame(ChannelGroup group){
        if(game!=null) return game;
        List<Player> players=new ArrayList<>();
        Iterator<Channel> it=group.iterator();
        while (it.hasNext()) {
            Player player=(Player)(it.next().attr(AttributeKey.valueOf("player")).get());
            players.add(player);
        }
        Game game=new NormalGame(players);
        // 初始化游戏数据: 发牌
        game.init();
        return game;
    }
    
}
