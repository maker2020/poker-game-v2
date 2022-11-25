package com.samay.netty.initializer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.samay.netty.handler.TextWebSocketFrameHandler;
import com.samay.netty.handler.UserDetailHandler;
import com.samay.netty.handler.game.GameReadyHandler;
import com.samay.netty.handler.game.MultipleHandler;
import com.samay.netty.handler.game.PutPokerHandler;
import com.samay.netty.handler.game.ReqBossHandler;
import com.samay.netty.handler.game.RoomReadyHandler;
import com.samay.netty.handler.game.TipPokerHandler;
import com.samay.netty.handler.heart.HeartHandler;
import com.samay.netty.handler.http.HttpRequestHandler;

/**
 * 服务器连接通道初始化<p>
 * <p>
 * 注意：单例handler(即@Sharable标记的handler)交由IoC容器管理其bean<p>
 * <ul>
 * <li>@Component等注解管理bean的作用域默认为单例</li>
 * <li>使用@Scope("prototype")声明多个实例</li>
 * </ul>
 * 
 */
@Component
public class ServerInitializer extends ChannelInitializer<Channel> {

    @Value("${netty.websocket.path}")
    private String wsUri;

    @Autowired
    private UserDetailHandler userDetailHandler;

    @Autowired
    private TextWebSocketFrameHandler textWebSocketFrameHandler;

    @Autowired
    private RoomReadyHandler roomReadyHandler;
    
    @Autowired
    private GameReadyHandler gameReadyHandler;
    
    @Autowired
    private ReqBossHandler reqBossHandler;
    
    @Autowired
    private PutPokerHandler putPokerHandler;

    @Autowired
    private TipPokerHandler tipPokerHandler;

    @Autowired
    private MultipleHandler multipleHandler;

    @Value("${netty.websocket.max-frame-size}")
    private int maxFrameSize;

    /**
     * 该类的成员handler单例/共享
     */
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline=ch.pipeline();
        pipeline.addLast(new HttpServerCodec()); // http解码
        pipeline.addLast(new ChunkedWriteHandler()); // 解决粘包/拆包
        pipeline.addLast(new HttpObjectAggregator(maxFrameSize));
        pipeline.addLast(new HttpRequestHandler(wsUri));
        pipeline.addLast(userDetailHandler);
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws",true));
        // 以上inboundhandler不需要解码，因此放在这里可以忽略以上handler的写数据
        // pipeline.addLast(textWebSocketFrameEncoder);

        pipeline.addLast(textWebSocketFrameHandler);
        pipeline.addLast(roomReadyHandler);
        pipeline.addLast(gameReadyHandler);
        pipeline.addLast(reqBossHandler);
        pipeline.addLast(putPokerHandler);
        pipeline.addLast(tipPokerHandler);
        pipeline.addLast(multipleHandler);
        pipeline.addLast(new IdleStateHandler(120, 120, 120));
        pipeline.addLast(new HeartHandler());
    }
    
}
