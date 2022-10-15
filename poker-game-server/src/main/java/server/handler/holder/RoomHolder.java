package server.handler.holder;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import game.NormalGame;
import game.entity.Player;
import game.entity.Room;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.ImmediateEventExecutor;

/**
 * 游戏房间持有对象
 */
public class RoomHolder {

    /**
     * 房间->ChannelGroup 的映射
     */
    public static Map<Room, ChannelGroup> roomChannelGroup = new ConcurrentHashMap<>();
    /**
     * 玩家ID->ChannelGroup 的映射
     */
    public static Map<String, ChannelGroup> playerChannelGroup = new ConcurrentHashMap<>();

    /**
     * 伪随机加入房间(默认按Map的key字符串字典排序)<p>
     * <b>该方法对线程安全、性能都有要求</b>
     * @param player
     */
    public static void randomJoinRoom(ChannelHandlerContext ctx) {
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
            synchronized(RoomHolder.class){
                ChannelGroup tempGroup = roomChannelGroup.get(room);
                if (tempGroup != null && tempGroup.size() < 3) {
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
            Random random = new Random(System.currentTimeMillis());
            String roomID = random.nextInt(100, 999) + "";
            room = new Room();
            // 初始化状态
            room.setId(roomID);
            room.addPlayer(player);
            // 设置游戏
            room.setGame(new NormalGame());
            
            // 绑定房间ID和ChannelGroup
            roomChannelGroup.put(room, group);

            group.add(ctx.channel());
        }
        room.getGame().addPlayer(player);
        // 将通道与房间绑定
        ctx.channel().attr(AttributeKey.valueOf("room")).set(room);
        // 绑定玩家ID和ChannelGroup
        playerChannelGroup.put(player.getName(), group);

    }

}
