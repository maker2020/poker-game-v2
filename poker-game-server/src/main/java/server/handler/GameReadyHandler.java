package server.handler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import game.Game;
import game.entity.Player;
import game.entity.Room;
import game.enums.GameStatusEnum;
import game.enums.RoomStatusEnum;
import io.netty.buffer.Unpooled;
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
 * 游戏准备阶段:<p>
 * 验证人数是否
 */
@Sharable
@Slf4j
public class GameReadyHandler extends SimpleChannelInboundHandler<Game> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,Game game) throws Exception {
        if(!canStart(game)) return;
        // 仅最后一个进入的线程处理发牌工作
        Player player=(Player)(ctx.channel().attr(AttributeKey.valueOf("player")).get());
        ChannelGroup group=RoomHolder.playerChannelGroup.get(player.getName());
        // 游戏发牌阶段
        Room room=(Room)(ctx.channel().attr(AttributeKey.valueOf("room")).get());
        log.info("RoomID["+room.getId()+"]:ready to start");
        // 更新房间 (此更新操作是明确的，没有线程安全问题)
        room.setStatus(RoomStatusEnum.START);
        // 初始化游戏、准备发牌    
        game=initGame(game);
        // 发牌至玩家(只有一个线程处理，所以需要分发group里的channel)
        // 注：这里嵌套循环数量级不大，并不影响性能，没必要在game实现类中建立维护hash结构存储变量提升性能。
        Iterator<Channel> it=group.iterator();
        while (it.hasNext()) {
            Channel ch=it.next();
            Player p=(Player)(ch.attr(AttributeKey.valueOf("player")).get());
            for(int i=0;i<game.getPlayers().size();i++){
                if(p.getName().equals(game.getPlayers().get(i).getName())){
                    Map<String,Object> msg=new HashMap<>();
                    msg.put("user", player.getName());
                    msg.put("pokers", player.getPokers());
                    ch.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(msg)));
                    break;
                }
            }
        }
        ctx.fireChannelRead(Unpooled.EMPTY_BUFFER);
    }

    private boolean canStart(Game game){
        boolean can=true;
        for(int i=0;i<game.getPlayers().size();i++){
            if(!game.getPlayers().get(i).isReady()) can=false;
        }
        return can;
    }

    /**
     * 初始化游戏相关
     * @param group
     * @param game (deprecated 注释)不必担心jmm的工作内存无法刷新至主存。即使没有volatile，sync块内变量，在锁释放之前都会刷新到主存
     * @return
     */
    private Game initGame(Game game){
        if(game.getStatus()==GameStatusEnum.START) return game;
        // 初始化游戏数据: 发牌、更新状态
        game.init();
        game.setStatus(GameStatusEnum.START);
        return game;
    }

    /**
     * 断连日志
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("disconnected: "+ctx.channel().remoteAddress());  
    }
    
}
