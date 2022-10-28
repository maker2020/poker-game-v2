package com.samay.netty.handler.http;

import com.samay.game.entity.Player;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import com.samay.netty.handler.holder.RoomManager;

/**
 * <b>Http处理类</b>(不共享实例)<p>
 * 该类是netty框架中定义的http处理类，仅用于过滤基于http的ws协议。
 * 也就是说channelRead()中仅仅会对ws连接握手的请求进行处理（玩家加入游戏房间）。<p>
 * <b>这意味着：</b>游戏房间外部的业务内容将由SpringBoot提供的web服务来处理。
 */
@Slf4j
@SuppressWarnings({ "deprecation" })
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private String wsUri;

    public HttpRequestHandler(String wsUri){
        this.wsUri=wsUri;
    }

    /* static{
        // http连接后，若player重复(不唯一)则抛出异常。
        AttributeKey.newInstance("player");
    } */

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (request.uri().startsWith(wsUri)) {
            // 游戏（连接房间）的用户于玩家信息的初始化。
            handleParams(ctx, request);
            ctx.fireChannelRead(request.retain());
        } else { // 非ws请求的处理
            if (HttpUtil.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }
            String responseData = "poker-game-v2 Server response";
            HttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(responseData.getBytes("UTF-8")));
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/json;charset=UTF-8");
            boolean keepAlive = HttpUtil.isKeepAlive(request);
            if (keepAlive) {
                response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, responseData.length());
                response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            }
            ctx.write(response);
            ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            if (!keepAlive) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
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
        String cloudID = decoder.parameters().get("cloudID").get(0);
        String nickName = decoder.parameters().get("nickName").get(0);
        if (cloudID == null || "".equals(cloudID) || !request.decoderResult().isSuccess()
                || !"websocket".equals(request.headers().get("Upgrade"))) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.BAD_REQUEST);
            ChannelFuture future = ctx.channel().writeAndFlush(response);
            future.addListener(ChannelFutureListener.CLOSE);
        }
        log.info("player["+nickName+"]@"+cloudID+" log in");
        // 初次实例化玩家
        Player player=new Player(cloudID);
        player.setNickName(nickName);
        initPlayerData(cloudID);
        // 将通道与玩家绑定
        ctx.channel().attr(AttributeKey.valueOf("player")).set(player);
        
        // 将该玩家随机加入一个房间
        RoomManager.randomJoinRoom(ctx);
    }

    /**
     * 可以是从数据库获取玩家相关数据，并初始化
     * @param playerID 指玩家唯一标识
     */
    private void initPlayerData(String playerID){

    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
