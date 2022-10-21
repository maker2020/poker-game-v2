package com.samay.netty.handler.holder;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.samay.game.NormalGame;
import com.samay.game.entity.Player;
import com.samay.game.entity.Room;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.ImmediateEventExecutor;
import com.samay.netty.exception.OverLoadException;

/**
 * <b>游戏房间管理器</b> (内存管理/回收的重点类对象)<p>
 * 包含玩家加入房间、初始化的相关参数绑定
 */
public class RoomManager {

    /**
     * 房间->ChannelGroup 的映射
     */
    private static Map<Room, ChannelGroup> roomChannelGroup = new ConcurrentHashMap<>();

    /**
     * 房间Set->ChannelGroup 的映射
     */
    private static Map<String, ChannelGroup> roomIDMapChannelGroup = new ConcurrentHashMap<>();

    /**
     * 服务器的房间数最大容量
     */
    public static final long MAX_ROOM_LENGTH = 20000L;
    public static AtomicLong currentRoomNums = new AtomicLong(0);

    /**
     * 伪随机加入房间(默认按Map的key字符串字典排序)
     * <p>
     * <b>该方法对线程安全、性能都有要求</b>
     * 
     * @param player
     */
    public static void randomJoinRoom(ChannelHandlerContext ctx) throws OverLoadException {
        Room room = null;

        Player player = (Player) (ctx.channel().attr(AttributeKey.valueOf("player")).get());
        Set<Room> roomSet = roomChannelGroup.keySet();
        Iterator<Room> it = roomSet.iterator();
        ChannelGroup group = null;
        // 判断是否存在房间(这里的线程互斥决定 -> 同时加入的玩家是否用同一个房间)
        // 这里不做处理，即真正意义上同时开始的玩家并不在一个房间
        while (it.hasNext()) {
            room = it.next();
            // 获取房间group并判断是否满足加入条件
            synchronized (RoomManager.class) {
                ChannelGroup tempGroup = roomChannelGroup.get(room);
                if (tempGroup != null && tempGroup.size() < 3 && room.getPlayers().size() < 3) {
                    group = tempGroup;
                    group.add(ctx.channel());
                    break;
                }
            }
        }
        // 未找到合适的group
        if (group == null || room == null) {
            // 实例化ChannelGroup和Room
            group = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
            String roomID = "";
            // 缺陷设计：(低性能)后续不采用随机的形式
            while (true) {
                Random random = new Random(System.currentTimeMillis());
                roomID = random.nextLong(0, 20001) + "";
                if (roomIDMapChannelGroup.containsKey(roomID))
                    continue;
                break;
            }
            long roomNums = currentRoomNums.getAndIncrement();
            if (roomNums > MAX_ROOM_LENGTH) {
                throw new OverLoadException("服务器满载，无法创建房间，稍后重试");
            }
            room = new Room();
            // 初始化状态
            room.setId(roomID);
            // 设置游戏
            room.setGame(new NormalGame());

            // 绑定房间ID和ChannelGroup
            roomChannelGroup.put(room, group);
            roomIDMapChannelGroup.put(roomID, group);

            group.add(ctx.channel());
        }
        // 公共
        room.addPlayer(player);
        room.getGame().addPlayer(player);


        // 通道范围 的绑定

        // 将通道与房间绑定
        ctx.channel().attr(AttributeKey.valueOf("room")).set(room);
        // 通道与group绑定
        ChannelHolder.groupMap.put(ctx.channel(), group);
        // 玩家唯一标识与ChannelID绑定
        ChannelHolder.uid_chidMap.put(player.getName(), ctx.channel().id());
    }

}
