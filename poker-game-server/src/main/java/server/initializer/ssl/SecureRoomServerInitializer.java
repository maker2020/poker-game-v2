package server.initializer.ssl;

import javax.net.ssl.SSLEngine;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import server.initializer.ServerInitializer;

/**
 * 安全层初始化类
 */
public class SecureRoomServerInitializer extends ServerInitializer {

    private final SslContext context;

    public SecureRoomServerInitializer(ChannelGroup group,SslContext context) {
        super(group);
        this.context=context;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception{
        super.initChannel(ch);
        SSLEngine engine=context.newEngine(ch.alloc());
        engine.setUseClientMode(false);
        ch.pipeline().addFirst(new SslHandler(engine));
    }
    
}
