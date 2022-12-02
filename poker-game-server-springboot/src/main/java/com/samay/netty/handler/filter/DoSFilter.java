package com.samay.netty.handler.filter;

import java.net.InetSocketAddress;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.samay.netty.handler.utils.RedisUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.ipfilter.RuleBasedIpFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * 防止DoS攻击的filter
 * <p>
 * 
 * 该类后续可根据需要设计更灵活，如注解、配置注入
 */
@Component
@Sharable
@Slf4j
public class DoSFilter extends RuleBasedIpFilter {

    private RedisUtil redisUtil;
    /**
     * 限制seconds之内
     */
    private final int seconds = 10;
    /**
     * 限制指定时间的最大请求次数
     */
    private final int maxReqTimes = 15;

    @Autowired
    public DoSFilter(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    protected boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress) throws Exception {
        try {
            String ip = remoteAddress.getHostString();
            // 校验是否为黑名单
            if(redisUtil.hasKey("BlackList", ip)){
                log.info("ip[" + remoteAddress.getHostString() + "] 已被拦截(黑名单ip)");
                return false;
            }
            String count = (String) redisUtil.hget("NettyDoSCount", ip);
            if (count == null) {
                redisUtil.hset("NettyDoSCount", ip, Integer.valueOf(1)+"", seconds);
                return true;
            } else {
                if (Integer.parseInt(count) < maxReqTimes) {
                    redisUtil.hset("NettyDoSCount", ip, (Integer.parseInt(count)+1)+"", seconds);
                    return true;
                }
            }
            log.warn("ip[" + remoteAddress.getHostString() + "] is attacking server !");   
            log.info("ip[" + remoteAddress.getHostString() + "] 已被加入黑名单");
            // 存入redis黑名单管理
            redisUtil.hset("BlackList", ip, new Date(System.currentTimeMillis()).toString());
        } catch (Exception e) {
            log.warn("ip[" + remoteAddress.getHostString() + "] is doing bad request !");
            return false;
        }
        return false;
    }

}
