package com.samay.game.entity;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.samay.game.enums.ActionEnum;
import com.samay.game.utils.TimerUtil;
import com.samay.game.vo.Notification;

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
public class Player extends User {
    
    private List<Poker> pokers=new LinkedList<>();
    private boolean boss=false;
    private boolean ready;
    /**
     * 是否已加注过
     */
    private boolean raise;

    /**
     * 记录玩家的操作
     */
    private Notification notification;
    
    
    // 游戏数据相关(user_data)
    /**
     * 游戏道具(超级加倍、记牌器)
     */
    private List<Item> items;
    /**
     * 游戏普通货币
     */
    private long freeMoney;
    /**
     * 游戏需充钱的货币
     */
    private transient long payMoney;
    /**
     * 胜场
     */
    private long winCount;
    /**
     * 败场
     */
    private long loseCount;
    /**
     * 经验(换算等级、称谓)
     */
    private long exp;
    /**
     * 是否断连(掉线)
     */
    private boolean disconnected;

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
    private transient boolean pass;

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
        setNotification(new Notification(ActionEnum.CALL, true));
        // 操作相关逻辑调用该方法以消除 限时检测的阻塞
        TimerUtil.checkTimeout(ActionEnum.CALL, getId());
    }

    public void askBoss() throws Exception{
        reqBoss();
        setNotification(new Notification(ActionEnum.ASK, true));
        TimerUtil.checkTimeout(ActionEnum.ASK, getId());
    }

    public void unCallBoss() throws Exception{
        refuseBoss();
        setNotification(new Notification(ActionEnum.CALL, false));
        TimerUtil.checkTimeout(ActionEnum.CALL, getId());
    }

    public void unAskBoss() throws Exception{
        refuseBoss();
        setNotification(new Notification(ActionEnum.ASK, false));
        TimerUtil.checkTimeout(ActionEnum.ASK, getId());
    }

    public void doubleMulti() throws Exception{
        setRaise(true);
        TimerUtil.checkTimeout(ActionEnum.MULTIPLE, getId());
        setNotification(new Notification(ActionEnum.DOUBLE, true));
    }

    public void doublePlusMulti() throws Exception{
        setRaise(true);
        TimerUtil.checkTimeout(ActionEnum.MULTIPLE, getId());
        setNotification(new Notification(ActionEnum.DOUBLE_PLUS, true));
    }

    public void refuseDouble() throws Exception{
        setRaise(true);
        TimerUtil.checkTimeout(ActionEnum.MULTIPLE, getId());
        setNotification(new Notification(ActionEnum.NO_DOUBLE, false));
    }

    public void putPokers(List<Poker> putPokers) throws Exception{
        if(putPokers!=null){
            removeAllPoker(putPokers);
        }
        TimerUtil.checkTimeout(ActionEnum.PUT, getId());
        setNotification(new Notification(ActionEnum.PUT, putPokers!=null, putPokers));
    }

    @Override
    public String toString(){
        return "player["+getNickName()+"]@"+getId();
    }
}
