package server.handler;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import com.alibaba.fastjson.JSON;

import game.Game;
import game.entity.Player;
import game.entity.Room;
import game.enums.ActionEnum;
import game.enums.GameStatusEnum;
import game.enums.RoomStatusEnum;
import game.vo.ResultVO;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import server.handler.holder.ChannelHolder;

/**
 * 游戏准备阶段:
 * <p>
 * 验证人数是否
 */
@Sharable
@Slf4j
public class GameReadyHandler extends SimpleChannelInboundHandler<Game> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Game game) throws Exception {
        if (!canStart(game))
            return;
        // 仅最后一个进入的线程处理发牌工作
        ChannelGroup group = ChannelHolder.groupMap.get(ctx.channel());
        // 游戏发牌阶段
        Room room = ChannelHolder.attrRoom(ctx.channel());
        log.info("RoomID[" + room.getId() + "]:ready to start");
        // 更新房间 (此更新操作是明确的，没有线程安全问题)
        room.setStatus(RoomStatusEnum.START);
        // 初始化游戏、准备发牌
        game = initGame(game);
        // 发牌至玩家(只有一个线程处理，所以需要分发group里的channel)
        // 注：这里嵌套循环数量级不大，并不影响性能，没必要在game实现类中建立维护hash结构存储变量提升性能。
        Iterator<Channel> it = group.iterator();
        while (it.hasNext()) {
            Channel ch = it.next();
            Player p = ChannelHolder.attrPlayer(ch);
            for (int i = 0; i < game.getPlayers().size(); i++) {
                if (p.getName().equals(game.getPlayers().get(i).getName())) {
                    Map<String, Object> msg = ResultVO.resultMap(p.getName(), p.getPokers());
                    ch.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(msg)));
                    break;
                }
            }
        }
        // 随机选一名玩家作为第一个叫地主的
        Random random = new Random(System.currentTimeMillis());
        Player randomPlayer = game.getPlayers().get(random.nextInt(0, 2));
        Map<String, Object> msg = ResultVO.resultMap(ActionEnum.CALL, randomPlayer.getName(), null);
        group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(msg)));

        // 辅助变量：维护players出牌顺序
        Collections.shuffle(room.getPlayers(), random); // 打乱players顺序

        // 至此结束，其他业务由其他handler从头处理
        ctx.fireChannelRead(Unpooled.EMPTY_BUFFER);
    }

    /**
     * 任意玩家没准备，返回false
     * <p>
     * game.players是安全容器，这里不用关注线程安全
     * 
     * @param game
     * @return
     */
    private boolean canStart(Game game) {
        boolean can = true;
        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (!game.getPlayers().get(i).isReady() || game.getPlayers().size() < 3)
                can = false;
        }
        return can;
    }

    /**
     * 初始化游戏相关
     * 
     * @param group
     * @param game  (deprecated
     *              注释)不必担心jmm的工作内存无法刷新至主存。即使没有volatile，sync块内变量，在锁释放之前都会刷新到主存
     * @return
     */
    private Game initGame(Game game) {
        if (game.getStatus() == GameStatusEnum.START)
            return game;
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
        log.warn("disconnected: " + ctx.channel().remoteAddress());
    }

}
