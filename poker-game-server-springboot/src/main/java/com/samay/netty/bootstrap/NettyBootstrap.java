package com.samay.netty.bootstrap;

import java.net.InetSocketAddress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * <b>Netty引导启动类</b>
 * 与SpringApplication主程序一起启动
 */
@Component
@Slf4j
public class NettyBootstrap implements ApplicationRunner,ApplicationListener<ContextClosedEvent>{
    
    @Value("${netty.websocket.port}")
    private int port;
    
    @Value("${netty.websocket.ip}")
    private String ip;

    @Autowired
    private ChannelInitializer<Channel> initializer;

    /**
     * server channel
     */
    private Channel channel;
    private final EventLoopGroup group = new NioEventLoopGroup();
    // 该子类EventLoopGroup管理EventLoop(包含IO处理的Channel)
    private final EventLoopGroup childGroup = new NioEventLoopGroup();

    @Override
    public void run(ApplicationArguments args) throws Exception{
        ChannelFuture future = start(new InetSocketAddress(port));
        log.info("Netty Server is starting on port: "+port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                destroy();
            }
        });
        future.channel().closeFuture().syncUninterruptibly();
    }

    public ChannelFuture start(InetSocketAddress address) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group,childGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(initializer);
        ChannelFuture future = bootstrap.bind(address);
        future.syncUninterruptibly();
        channel = future.channel();
        return future;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        if(channel!=null){
            channel.close();
        }
        log.info("netty server is closed.");
    }

    public void destroy() {
        if (channel != null) {
            channel.close();
        }
        group.shutdownGracefully();
    }

}
