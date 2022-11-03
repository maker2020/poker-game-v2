package com.samay.netty.handler;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

import com.samay.game.Game;
import com.samay.game.entity.Player;
import com.samay.game.entity.Room;
import com.samay.game.enums.ActionEnum;
import com.samay.game.enums.RoomStatusEnum;
import com.samay.game.vo.ResultVO;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import com.samay.netty.handler.holder.ChannelHolder;

/**
 * 游戏准备阶段:
 * <p>
 * 验证人数是否
 */
@Sharable
@Component
@Slf4j
public class GameReadyHandler extends SimpleChannelInboundHandler<Game> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Game game) throws Exception {
        if (!canStart(game))
            return;
        // 游戏发牌阶段: 仅最后一个进入的线程处理发牌工作
        ChannelGroup group = ChannelHolder.groupMap.get(ctx.channel());
        Room room = ChannelHolder.attrRoom(ctx.channel());
        log.info("RoomID[" + room.getId() + "]:ready to start");

        // 更新房间 (此更新操作是明确的，没有线程安全问题)
        room.setStatus(RoomStatusEnum.START);

        gameStart(game, group, room, ctx);
    }

    /**
     * 游戏开始: 发牌、随机开始询问开始叫地主。（其他的业务不在这里）
     * @param game
     * @param group
     * @param room
     * @param ctx
     */
    public static void gameStart(Game game,ChannelGroup group,Room room,ChannelHandlerContext ctx){
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
    private static Game initGame(Game game) {
        // 初始化游戏数据: 发牌、更新状态
        game.init();
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