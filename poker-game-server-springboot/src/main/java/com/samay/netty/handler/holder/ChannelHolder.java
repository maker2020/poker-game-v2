package com.samay.netty.handler.holder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.samay.game.entity.Player;
import com.samay.game.entity.Room;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.AttributeKey;

/**
 * <b>通道全局持久访问类</b>(内存管理/回收的重点类对象)<p>
 * 包含通道相关的快速访问以及相关属性
 */
public class ChannelHolder {
    
    /**
     * 通道->group的映射
     */
    public static Map<Channel,ChannelGroup> groupMap=new ConcurrentHashMap<>();

    /**
     * <b>玩家唯一标识->ChannelID映射</b><p>
     * 维护目的：快速从group定位玩家channel
     */
    public static Map<String,ChannelId> uid_chidMap=new ConcurrentHashMap<>();

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
