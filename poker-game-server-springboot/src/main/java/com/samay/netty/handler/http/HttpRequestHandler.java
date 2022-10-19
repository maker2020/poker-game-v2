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
 * <b>Http处理类</b><p>
 * 不共享实例
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
            // 初始化和游戏相关的请求
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

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 处理用户(含初始化参数)的请求。<p>
     * <b>初始化玩家信息。并随机加入一个房间:ChannelGroup</b>
     * @param ctx
     * @param request
     */
    private void handleParams(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        String username = decoder.parameters().get("username").get(0);
        if (username == null || "".equals(username) || !request.decoderResult().isSuccess()
                || !"websocket".equals(request.headers().get("Upgrade"))) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.BAD_REQUEST);
            ChannelFuture future = ctx.channel().writeAndFlush(response);
            future.addListener(ChannelFutureListener.CLOSE);
        }
        log.info("player["+username+"] log in");
        // 初次实例化玩家
        Player player=new Player(username);
        initPlayerData(username);
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

}
