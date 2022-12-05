package com.samay.netty.handler.aop;

import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import com.samay.game.Game;
import com.samay.game.bo.Player;

/**
 * game中玩家action行为的切面<p>
 * 
 * 由于大量实例未交给Spring管理，暂时用不上SpringAOP。简单封装一下即可<p>
 * 
 * 该类同级包test下的类可以被抽象定义为AOP中的切面
 */
@Aspect
@Component
public class ActingPlayerAspect {
    
    /**
     * 切点在每次游戏中的actingPlayer变化的时候——即调用set方法更新时
     */
    @Pointcut("execution(* com.samay.game..*.*(..))")
    private void setActingPlayerCut(){}

    @After("setActingPlayerCut()")
    public void setActingPlayerAfter(JoinPoint jp) throws Throwable{
        Game game=(Game)jp.getTarget();
        // 轮到某个玩家act,则清除其notification
        List<Player> players=game.getPlayers();
        for(Player p:players){
            if(p.getId().equals(game.getActingPlayer())){
                p.setNotification(null);
                break;
            }
        }
    }

}
