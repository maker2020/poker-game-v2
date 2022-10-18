package server.initializer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import server.handler.GameReadyHandler;
import server.handler.PutPokerHandler;
import server.handler.ReqBossHandler;
import server.handler.RoomReadyHandler;
import server.handler.TextWebSocketFrameHandler;
import server.handler.heart.HeartHandler;
import server.handler.http.HttpRequestHandler;

/**
 * 服务器连接通道初始化
 */
public class ServerInitializer extends ChannelInitializer<Channel> {

    private RoomReadyHandler roomReadyHandler;
    private GameReadyHandler gameReadyHandler;
    private ReqBossHandler reqBossHandler;
    private PutPokerHandler putPokerHandler;

    public ServerInitializer(){
        this.roomReadyHandler=new RoomReadyHandler();
        this.gameReadyHandler=new GameReadyHandler();
        this.reqBossHandler=new ReqBossHandler();
        this.putPokerHandler=new PutPokerHandler();
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline=ch.pipeline();
        pipeline.addLast(new HttpServerCodec()); // http解码
        pipeline.addLast(new ChunkedWriteHandler()); // 解决粘包/拆包
        pipeline.addLast(new HttpObjectAggregator(256*1024));
        pipeline.addLast(new HttpRequestHandler("/ws"));
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws",true));
        pipeline.addLast(new TextWebSocketFrameHandler());
        // 以下handler单例共享
        pipeline.addLast(roomReadyHandler);
        pipeline.addLast(gameReadyHandler);
        pipeline.addLast(reqBossHandler);
        pipeline.addLast(putPokerHandler);
        pipeline.addLast(new IdleStateHandler(120, 120, 120));
        pipeline.addLast(new HeartHandler());
    }
    
}
