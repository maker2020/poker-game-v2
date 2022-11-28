package com.samay.netty.handler.holder;

import java.util.Iterator;
import java.util.Set;

import com.samay.game.entity.Player;
import com.samay.game.entity.Room;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.AttributeKey;

/**
 * <b>通道全局持久访问类</b>(内存管理/回收的重点类对象)<p>
 * 包含通道相关的快速访问以及相关属性
 */
public class ChannelHolder {

    /**
     * 获取channel绑定的玩家。<p>
     * 
     * @param ch
     * @return 玩家
     */
    public static Player attrPlayer(Channel ch){
        if(ch==null) return null;
        Object obj=ch.attr(AttributeKey.valueOf("player")).get();
        if(obj instanceof Player){
            Player player=(Player)obj;
            return player;
        }
        return null;
    }

    /**
     * 获取channel绑定的房间。<p>
     * 
     * @param ch
     * @return
     */
    public static Room attrRoom(Channel ch){
        if(ch==null) return null;
        Object obj=ch.attr(AttributeKey.valueOf("room")).get();
        if(obj instanceof Room){
            Room room=(Room)obj;
            return room;
        }
        return null;
    }

    /**
     * 获取channel对应的group
     * @param ch
     * @return
     */
    public static ChannelGroup getGroup(Channel ch){
        Room room=attrRoom(ch);
        return RoomManager.roomChannelGroup.get(room);
    }

    /**
     * 获取room对应的group
     * @param room
     * @return
     */
    public static ChannelGroup getGroup(Room room){
        return RoomManager.roomChannelGroup.get(room);
    }

    /**
     * 从所有Channel中查找,并获取该playerID对应的Channel<p>
     * 
     * 注意服务器已维护channel->player(hash)，但反向若不做hash表，查找复杂度为O(n)。
     * <b>另外由于ChannelGroup是Set的多态实现，可以复用getChannel(Set<Channel>,String)</b>
     * @param playerID
     * @return
     */
    public static Channel getChannel(String playerID){
        Set<Channel> channels=RoomManager.getAllChannels();
        return getChannel(channels, playerID);
    }

    /**
     * 通过指定group范围，查找playerID对应的Channel<p>
     * 
     * 注意观察发现ChannelGroup就是Set的子类
     * @param group
     * @param playerID
     * @return
     */
    public static Channel getChannel(Set<Channel> group,String playerID){
        Iterator<Channel> it=group.iterator();
        while (it.hasNext()) {
            Channel ch=it.next();
            Player p=attrPlayer(ch);
            if(p!=null && p.getId().equals(playerID)){
                return ch;
            }
        }
        return null;
    }

}
