package server.handler.holder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
    public static Map<Room, ChannelGroup> roomChannelGroup = new HashMap<>();
    /**
     * 玩家ID->ChannelGroup 的映射
     */
    public static Map<String, ChannelGroup> playerChannelGroup = new HashMap<>();

    /**
     * 伪随机加入房间(默认按Map的key字符串字典排序)
     * 
     * @param player
     */
    public static void randomJoinRoom(ChannelHandlerContext ctx) {
        Room room = null;

        Player player = (Player) (ctx.channel().attr(AttributeKey.valueOf("player")).get());
        Set<Room> roomIDSet = roomChannelGroup.keySet();
        Iterator<Room> it = roomIDSet.iterator();
        ChannelGroup group = null;
        while (it.hasNext()) {
            room = it.next();
            ChannelGroup tempGroup = roomChannelGroup.get(room);
            if (tempGroup != null && tempGroup.size() < 3) {
                group = tempGroup;
                break;
            }
        }
        if (group == null || room == null) {
            // 实例化ChannelGroup和Room
            group = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
            Random random = new Random();
            String roomID = random.nextInt(100, 999) + "";
            room = new Room();
            // 初始化状态
            room.setId(roomID);
            room.addPlayer(player);
            
            // 绑定房间ID和ChannelGroup
            roomChannelGroup.put(room, group);
        }
        // 将通道与房间绑定
        ctx.channel().attr(AttributeKey.valueOf("room")).set(room);
        // 绑定玩家ID和ChannelGroup
        playerChannelGroup.put(player.getName(), group);

        group.add(ctx.channel());
    }

}
