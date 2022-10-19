package server;

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import server.initializer.ServerInitializer;

/**
 * 服务器开启入口类
 */
@Slf4j
class Server {

    private final EventLoopGroup group = new NioEventLoopGroup();
    // 该子类EventLoopGroup管理EventLoop(包含IO处理的Channel)
    private final EventLoopGroup childGroup = new NioEventLoopGroup();
    private Channel channel;

    public ChannelFuture start(InetSocketAddress address) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group,childGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(createInitializer());
        ChannelFuture future = bootstrap.bind(address);
        future.syncUninterruptibly();
        channel = future.channel();
        return future;
    }

    protected ChannelInitializer<Channel> createInitializer() {
        return new ServerInitializer();
    }

    public void destroy() {
        if (channel != null) {
            channel.close();
        }
        group.shutdownGracefully();
    }

    public static void main(String[] args) {
        int port=8888;
        final Server endpoint = new Server();
        ChannelFuture future = endpoint.start(
                new InetSocketAddress(port));
        log.info("Server is starting on port: "+port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                endpoint.destroy();
            }
        });
        future.channel().closeFuture().syncUninterruptibly();
    }

}


/**
 * 程序入口类(安全类。包含SSL层，访问要求https)
 */
public class SecureServer extends Server {

    /* private final SslContext context;

    public SecureServer(SslContext context) {
        this.context = context;
    } */

    /* @Override
    protected ChannelInitializer<Channel> createInitializer(ChannelGroup group) {
        return new SecureRoomServerInitializer(group, context);
    } */

    /* private static void main(String[] args) throws Exception{
        SelfSignedCertificate cert = new SelfSignedCertificate();
        // 这里Ssl体系加密只适于测试环境
        SslContext context = SslContextBuilder.forServer(cert.certificate(), cert.privateKey()).build();
        final SecureServer endpoint = new SecureServer(context);
        ChannelFuture future = endpoint.start(new InetSocketAddress(8888));
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                endpoint.destroy();
            }
        });
        future.channel().closeFuture().syncUninterruptibly();
    } */

}