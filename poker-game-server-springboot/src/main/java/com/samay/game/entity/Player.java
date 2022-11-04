package com.samay.game.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
@EqualsAndHashCode(of="name")
public class Player implements Serializable{
    /**
     * 玩家唯一标识(name/id等)，而非玩家昵称
     */
    private String name;
    /**
     * 玩家展示的昵称
     */
    private String nickName;
    /**
     * 0:未设置，1:男，2:女
     */
    private char sex='1';
    private List<Poker> pokers=new LinkedList<>();
    private boolean boss=false;
    private boolean ready;

    // 辅助变量
    /**
     * 用于判断出地主
     */
    private int reqTimes=0;
   
    /**
     * 是否拒绝过当地主
     */
    private boolean refuseBoss=false;

    /**
     * 请求的索引
     */
    private int reqIndex=0;

    /**
     * 是否是叫地主的/第一个叫
     */
    private boolean firstCall=false;

    
    /**
     * 以下为v1的辅助变量，现在已不使用，先保留<p>
     * 是否pass
     */
    @Deprecated
    private boolean pass;

    /**
     * 按照玩家唯一标识(cloudID/id等)
     * @param name 唯一标识
     */
    public Player(String name){
        this.name=name;
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

    public void reqBoss(){
        reqTimes++;
    }

    public void refuseBoss(){
        refuseBoss=true;
    }

    @Override
    public String toString(){
        return "player[name:"+name+"]";
    }
}
