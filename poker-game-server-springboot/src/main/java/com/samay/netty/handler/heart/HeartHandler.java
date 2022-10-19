package com.samay.netty.handler.heart;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 代表读写空闲
        if (evt instanceof IdleState) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if(idleStateEvent.state()==IdleState.READER_IDLE){
                log.debug("读空闲...");
            }else if(idleStateEvent.state()==IdleState.WRITER_IDLE){
                log.debug("写空闲...");
            }else if(idleStateEvent.state()==IdleState.ALL_IDLE){
                log.debug("读写空闲...");
                // 关闭没有ping/pong的Channel
                Channel channel=ctx.channel();
                channel.close();
            }
        }
    }

}
