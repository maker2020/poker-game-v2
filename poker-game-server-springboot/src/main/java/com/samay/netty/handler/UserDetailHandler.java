package com.samay.netty.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.samay.game.entity.Player;
import com.samay.game.entity.User;
import com.samay.netty.handler.holder.RoomManager;
import com.samay.service.UserService;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理用户/玩家资料的业务类
 */
@Component
@Sharable
@Slf4j
public class UserDetailHandler extends SimpleChannelInboundHandler<FullHttpRequest>{

    private UserService userService;

    /**
     * 构造注入以更好的关注分离点或是否需要重构类
     * @param userService
     */
    @Autowired
    public UserDetailHandler(UserService userService){
        this.userService=userService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        handleParams(ctx, request);
        ctx.fireChannelRead(request.retain());
    }    

    /**
     * 处理用户于玩家加入游戏(含初始化参数)的请求。<p>
     * <b>初始化信息。并随机加入一个房间:ChannelGroup</b><p>
     * 具体参数包含：
     * <ul>
     *  <li>(必须)用户唯一标识,即name/id等)</li>
     *  <li>用户于玩家昵称</li>
     * </ul>
     * @param ctx
     * @param request
     */
    private void handleParams(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        String userInfo = decoder.parameters().get("user").get(0);
        if (userInfo == null || "".equals(userInfo) || !request.decoderResult().isSuccess()
                || !"websocket".equals(request.headers().get("Upgrade"))) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.BAD_REQUEST);
            ChannelFuture future = ctx.channel().writeAndFlush(response);
            future.addListener(ChannelFutureListener.CLOSE);
            return;
        }
        // 客户端登入的用户信息
        User user=JSON.parseObject(userInfo,User.class);
        if(user==null) return;
        log.info(user.toString()+" log in");
        // 初次实例化玩家
        Player player=initPlayerData(user);
        // 将通道与玩家绑定
        ctx.channel().attr(AttributeKey.valueOf("player")).set(player);
        
        // 将该玩家随机加入一个房间
        RoomManager.randomJoinRoom(ctx);
    }

    /**
     * 可以是从数据库获取玩家相关数据，并初始化
     * @param playerID 指玩家唯一标识
     */
    private Player initPlayerData(User user){
        Player player=userService.findPlayerByID(user.getId());
        if(player==null){ // 直接注册
            userService.register(user);
            player=userService.findPlayerByID(user.getId());
        }
        return player;
    }

}

