package com.samay.netty.handler.utils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.samay.game.entity.Player;
import com.samay.game.entity.Poker;
import com.samay.game.entity.Room;
import com.samay.game.enums.PokerColorEnum;
import com.samay.game.enums.PokerValueEnum;
import com.samay.game.utils.RV;
import com.samay.netty.handler.holder.ChannelHolder;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * 写操作的封装
 */
public class WriteUtil {
    
    /**
     * <b>传输游戏房间内数据——RoomData给客户端</b><p>
     * 针对房间内不同Channel/玩家过滤数据的writeAndFlush (例如其他玩家手牌)
     * @param group
     * @param message
     * @throws Exception
     */
    public static void writeAndFlushRoomDataByFilter(ChannelGroup group) throws Exception{
        Iterator<Channel> it = group.iterator();
        while (it.hasNext()) {
            Channel ch = it.next();
            Player player = ChannelHolder.attrPlayer(ch);
            Room copyRoom=ChannelHolder.attrRoom(ch).deepClone();
            List<Player> players=copyRoom.getPlayers();
            for(Player p:players){
                if(!p.getId().equals(player.getId())){
                    // 屏蔽其他玩家的手牌 (移除/或用无效数据)
                    List<Poker> list=p.getPokers();
                    Collections.fill(list, new Poker(PokerColorEnum.HEART, PokerValueEnum.BLANK));
                }
            }
            ch.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(RV.roomData(copyRoom),SerializerFeature.DisableCircularReferenceDetect))); 
        }
    }

    /**
     * 将message以JSON格式封装到TextWebSocketFrame,并由group发送到客户端
     * 
     * @param message
     */
    public static void writeAndFlushTextWebSocketFrame(ChannelGroup group,Object message){
        group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(message,SerializerFeature.DisableCircularReferenceDetect)));
    }

}
