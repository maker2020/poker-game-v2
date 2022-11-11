package com.samay.game.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.samay.game.enums.ActionEnum;
import com.samay.game.utils.TimerUtil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 玩家类(BO：游戏业务对象)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Player extends User implements Serializable{
    
    private List<Poker> pokers=new LinkedList<>();
    private boolean boss=false;
    private boolean ready;
    /**
     * 是否已加注过
     */
    private boolean raise;

    // 辅助变量，大多不会序列化(如使用jackson、jdk、msgpack、google等序列化框架)/传输这些字段
    /**
     * 用于判断出地主
     */
    private transient int reqTimes=0;
   
    /**
     * 是否拒绝过当地主
     */
    private transient boolean refuseBoss=false;

    /**
     * 请求的索引
     */
    private transient int reqIndex=0;

    /**
     * 是否是叫地主的/第一个叫
     */
    private transient boolean firstCall=false;

    
    /**
     * 以下为v1的辅助变量，现在已不使用，先保留<p>
     * 是否pass
     */
    @Deprecated
    private boolean pass;

    /**
     * 按照玩家唯一标识(cloudID/id等)
     * @param id 唯一标识
     */
    public Player(String id){
        super.setId(id);
    }

    public void addPoker(Poker poker){
        pokers.add(poker);
    }

    public void addAllPoker(Collection<Poker> listPoker){
        pokers.addAll(listPoker);
    }

    public void removePoker(Poker poker){
        pokers.remove(poker);
    }

    public void removeAllPoker(Collection<Poker> listPoker){
        pokers.removeAll(listPoker);
    }

    private void reqBoss(){
        reqTimes++;
    }

    private void refuseBoss(){
        refuseBoss=true;
    }

    public void callBoss() throws Exception{
        setFirstCall(true);
        reqBoss();
        // 操作相关逻辑调用该方法以消除 限时检测的阻塞
        TimerUtil.checkTimeout(ActionEnum.CALL, getId());
    }

    public void askBoss() throws Exception{
        reqBoss();
        TimerUtil.checkTimeout(ActionEnum.ASK, getId());
    }

    public void unCallBoss() throws Exception{
        refuseBoss();
        TimerUtil.checkTimeout(ActionEnum.CALL, getId());
    }

    public void unAskBoss() throws Exception{
        refuseBoss();
        TimerUtil.checkTimeout(ActionEnum.ASK, getId());
    }

    @Override
    public String toString(){
        return "player[id:"+getId()+"]";
    }
}
