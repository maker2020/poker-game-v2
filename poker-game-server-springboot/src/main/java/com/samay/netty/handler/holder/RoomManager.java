package com.samay.netty.handler.holder;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.samay.game.Game;
import com.samay.game.NormalGame;
import com.samay.game.entity.Player;
import com.samay.game.entity.Room;
import com.samay.game.enums.GameStatusEnum;
import com.samay.game.enums.RoomStatusEnum;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.ImmediateEventExecutor;
import lombok.extern.slf4j.Slf4j;

import com.samay.netty.exception.OverLoadException;

/**
 * <b>游戏房间管理器</b> (内存管理/回收的重点类对象)
 * <p>
 * 包含玩家加入房间、初始化的相关参数绑定<p>
 * 以下是服务器内游戏相关的所有绑定关系
 * <ul>
 * <li>Channel -> Player</li>
 * <li>Room -> Group, Group -> Channel ,(RoomID->Group)</li>
 * <li>Channel -> Room</li>
 * </ul>
 * 
 * 另外,内置守护线程间隔性清理非运行状态的房间
 */
@Slf4j
public class RoomManager {

    /**
     * 房间->ChannelGroup 的映射<p>
     * 
     * protected修饰,取相关数据应调用ChannelHolder中的公有方法
     */
    protected static Map<Room, ChannelGroup> roomChannelGroup = new ConcurrentHashMap<>();

    /**
     * 房间Set->ChannelGroup 的映射
     */
    private static Map<String, ChannelGroup> roomIDMapChannelGroup = new ConcurrentHashMap<>();

    /**
     * 服务器的房间数最大容量
     */
    public static final long MAX_ROOM_LENGTH = 20000L;
    public static AtomicLong currentRoomNums = new AtomicLong(0);

    static {
        Thread roomClearThread=new Thread(()->{
            log.info("room manager cleaner is running.");
            while (true && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(20000);
                    Set<Room> roomSet=getAllRooms();
                    Iterator<Room> it=roomSet.iterator();
                    while (it.hasNext()) {
                        Room room=it.next();
                        if(room.getStatus()!=RoomStatusEnum.START){ // 房间并没有在进行游戏
                            // 指定时间后再次判断该房间状态，若仍未开始，则释放
                            long time=room.getPlayers().size()>0?30:5;
                            ScheduledExecutorService service=Executors.newSingleThreadScheduledExecutor();
                            service.schedule(()->{
                                if(room.getStatus()!=RoomStatusEnum.START){
                                    // 断开玩家的连接
                                    disconnectPlayers(room);
                                    // 清除房间
                                    releaseRoom(room);
                                }
                            }, time, TimeUnit.SECONDS);
                            service.shutdown(); // 拒绝新任务
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            log.info("room manager cleaner is closing.");
        });
        roomClearThread.setDaemon(true);
        roomClearThread.setName("Room-Cleaner");
        roomClearThread.start();
    }

    /**
     * 伪随机加入房间(默认按Map的key字符串字典排序)
     * <p>
     * <b>该方法对线程安全、性能都有要求</b>
     * 
     * @param player
     */
    public static void randomJoinRoom(ChannelHandlerContext ctx) throws OverLoadException {
        Player player = (Player) (ctx.channel().attr(AttributeKey.valueOf("player")).get());
        Room unfinishedGameRoom=getUnfinished(player.getId());
        if(unfinishedGameRoom!=null){ // 恢复其对局
            // 刷新channel相关绑定
            ChannelGroup group=roomChannelGroup.get(unfinishedGameRoom);
            // 之前断开的channel会被unregistered、释放
            group.add(ctx.channel());
            Player pastPlayer=null;
            for(Player p:unfinishedGameRoom.getPlayers()){
                if(p.getId().equals(player.getId())){
                    pastPlayer=p;
                    pastPlayer.setDisconnected(false);
                    break;
                }
            }
            // 将对局中的player赋值给channel
            ctx.channel().attr(AttributeKey.valueOf("player")).set(pastPlayer);
            ctx.channel().attr(AttributeKey.valueOf("room")).set(unfinishedGameRoom);
            return;
        }
        Room room = null;
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

        // 将通道与房间绑定
        ctx.channel().attr(AttributeKey.valueOf("room")).set(room);
    }

    /**
     * 检测该id玩家是否存在未完成(正在进行->Status==START)的对局
     * @param playerID
     * @return
     */
    private static Room getUnfinished(String playerID){
        // 遍历Room
        Set<Room> roomSet=roomChannelGroup.keySet();
        Iterator<Room> it=roomSet.iterator();
        while (it.hasNext()) {
            Room room=it.next();
            Game game=room.getGame();
            if(room!=null && game!=null && game.getStatus()==GameStatusEnum.START){
                List<Player> players=room.getPlayers();
                for(Player p:players){
                    if(p.getId().equals(playerID)){
                        return room;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取服务器中 pokergame所有的房间
     * 
     * @return
     */
    public static Set<Room> getAllRooms() {
        return roomChannelGroup.keySet();
    }

    /**
     * 获取服务器中 pokergame所有的连接通道(channel)
     * 
     * @return
     */
    public static Set<Channel> getAllChannels() {
        Set<Room> rooms = getAllRooms();
        Set<Channel> allChannels = new HashSet<>();
        Iterator<Room> it = rooms.iterator();
        while (it.hasNext()) {
            Room room = it.next();
            ChannelGroup group = roomChannelGroup.get(room);
            Set<Channel> channels = group.stream().collect(Collectors.toSet());
            allChannels.addAll(channels);
        }
        return allChannels;
    }

    /**
     * 断开房间玩家的连接
     * 
     * @param room
     */
    private static void disconnectPlayers(Room room){
        ChannelGroup group=roomChannelGroup.get(room);
        if(group!=null){
            group.forEach((ch)->{
                Player player=ChannelHolder.attrPlayer(ch);
                ChannelFuture future=ch.close();
                future.addListener((res)->{
                    if(res.isSuccess()){
                        log.info(player.toString()+" 已被服务器断开连接.");
                    }
                });
            });
        }
    }

    /**
     * 释放房间相关资源
     * 
     * @param room
     */
    private static void releaseRoom(Room room){
        log.info("room["+room.getId()+"] is releasing.");
        // 释放玩家: 清除玩家对象在服务器中被生命周期较长的引用(容器)所指向
        room.clearPlayers();
        // 释放group(当调用remove时，mapping关系会被断开，map会释放key与value的地址的引用)
        roomChannelGroup.remove(room);
        roomIDMapChannelGroup.remove(room.getId());
        log.info("room["+room.getId()+"] has been released.");
    }

}
