package server.handler.holder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import game.entity.Player;
import game.entity.Room;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.AttributeKey;

/**
 * 通道全局持久访问类
 */
public class ChannelHolder {
    
    /**
     * 通道->group的映射
     */
    public static Map<Channel,ChannelGroup> groupMap=new ConcurrentHashMap<>();

    /**
     * 获取通道绑定的玩家。<p>
     * 方法实现封装了Channel.attr(AttributeKey.valueOf()).get()
     * @param ch
     * @return 玩家
     */
    public static Player attrPlayer(Channel ch){
        Player player=(Player)(ch.attr(AttributeKey.valueOf("player")).get());
        return player;
    }

    /**
     * 获取通道绑定的房间。<p>
     * 方法实现封装了Channel.attr(AttributeKey.valueOf()).get()
     * @param ch
     * @return
     */
    public static Room attrRoom(Channel ch){
        Room room=(Room)(ch.attr(AttributeKey.valueOf("room")).get());
        return room;
    }

}
