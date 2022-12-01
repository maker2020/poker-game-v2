package com.samay.netty.handler.filter;

import java.net.InetSocketAddress;

import org.springframework.stereotype.Component;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.ipfilter.RuleBasedIpFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * 防止DoS攻击的filter
 */
@Component
@Sharable
@Slf4j
public class DoSFilter extends RuleBasedIpFilter{

    @Override
    protected boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress) throws Exception {
        log.info("ip["+remoteAddress.getHostString()+"]");
        return true;
    }

}
