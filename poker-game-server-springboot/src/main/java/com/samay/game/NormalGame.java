package com.samay.game;

import com.samay.game.entity.Player;
import com.samay.game.entity.Poker;
import com.samay.game.enums.GameStatusEnum;
import com.samay.game.enums.PokerColorEnum;
import com.samay.game.enums.PokerValueEnum;
import com.samay.game.rule.CommonRule;
import com.samay.game.utils.PokerUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 普通的游戏模式
 */
@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class NormalGame extends Game{

    public NormalGame(){
        restart();
    }

    public synchronized void init(){
        if(getStatus()==GameStatusEnum.START) return;
        if(getStatus()!=GameStatusEnum.START) handOutPokers();
        setStatus(GameStatusEnum.START);
    }

    /**
     * if return null,it means that over one player called Boss
     * thread safe,the field {@code count} is shared.<p>
     * <p>
     * 这里v2版本逻辑修改了，将player的refuse加入了判断逻辑中
     * @return
     */
    public Player getBossInstantly(){
        if(getTurnCallIndex().get()<3) return null;
        Player player=null;
        int most=0;
        for(Player p:getPlayers()){
            if(p.isRefuseBoss()){
                most=Math.max(most, 0);
            }else{
                most=Math.max(most, p.getReqTimes());
            }
        }
        // 栈内不共享，因此不担心原子操作和线程安全
        int count=0;
        for(Player p:getPlayers()){
            if(most==p.getReqTimes() && !p.isRefuseBoss()) {
                count++;
                player=p;
            }
            if(count>1) player=null;
        }
        if(player!=null) player.setBoss(true);
        else{
            if(getTurnCallIndex().get()>3){ // 意味着最后决定已经做出，可以得出地主
                int resIndex=0;
                for(Player p:getPlayers()){
                    if(p.isFirstCall()){
                        // 只有第一个叫的人才可以进入该判断，地主是该玩家的上一个人
                        resIndex=p.getReqIndex()-1;                 
                    }
                }
                for(Player p:getPlayers()){
                    if(p.getReqIndex()==resIndex){
                        return p;
                    }
                }
            }
        }
        return player;
    }

    /**
     * 随机顺序发牌
     */
    protected void handOutPokers(){
        if(getPokerCollector()==null) setPokerCollector(new LinkedList<>());
        if(getPokerBossCollector()==null) setPokerBossCollector(new LinkedList<>());
        for(int i=0;i<13;i++){
            for(int j=0;j<4;j++){
                getPokerCollector().add(new Poker(PokerColorEnum.getByCode(j+1), PokerValueEnum.getByCode(i+1)));
            }    
        }
        getPokerCollector().add(new Poker(PokerColorEnum.HEART, PokerValueEnum.King));
        getPokerCollector().add(new Poker(PokerColorEnum.SPADE, PokerValueEnum.Queen));
        // 放List打乱顺序
        List<Poker> pokerList=new ArrayList<>();
        pokerList.addAll(getPokerCollector());
        Collections.shuffle(pokerList);
        int handIndex=0;
        for(Poker poker:pokerList){
            if(handIndex<=50)
                getPlayers().get(handIndex%3).addPoker(poker);
            else
                getPokerBossCollector().add(poker);
            handIndex++;
        }

        PokerUtil.sortForPUT((LinkedList<Poker>)getPokerBossCollector());
        // 同花顺或大小王加倍
        if(CommonRule.flush(getPokerBossCollector())){
            setMultiple(getMultiple()*2);
        }

        for(Player p:getPlayers()){
            PokerUtil.sort(p.getPokers());
        }
    }

    /**
     * 重置游戏状态/初始化游戏状态
     */
    @Override
    public void restart() {
        this.setPokerBossCollector(null);
        this.setPokerCollector(null);
        this.setStatus(GameStatusEnum.READY);
        this.getTurnCallIndex().set(0);
        // 玩家于游戏中的一些状态变量的重置
        for(Player p:getPlayers()){
            p.setBoss(false);
            p.setFirstCall(false);
            p.setReady(false);
            p.setRefuseBoss(false);
            p.setReqIndex(0);
            p.setReqTimes(0);
            p.getPokers().clear();
        }
        
        this.setLastPutPokers(null);
        this.setLastPlayerID("");
        this.setActingPlayer("");

        this.setBaseScore(200);
        this.setCardinality(2);
        this.setMultiple(2);
    }

    /**
     * 结算（未考虑货币不够的情况，直接为负数）
     */
    @Override
    public Map<String,Object> settlement() {
        List<Player> winnerList=new ArrayList<>();
        List<Player> loserList=new ArrayList<>();
        for(Player p:getPlayers()){
            if(p.getPokers().size()==0){
                winnerList.add(p);
            }
        }
        if(winnerList.size()==0) return null;
        Player winner=winnerList.get(0);
        if(!winner.isBoss()){
            for(Player p:getPlayers()){
                if(p==winner) continue;
                if(!p.isBoss()){
                    winnerList.add(p);
                }else{
                    loserList.add(p);
                }
            }
        }else{
            for(Player p:getPlayers()){
                if(!p.isBoss()){
                    loserList.add(p);
                }
            }
        }

        // 首先根据底分(该NormalGame是200) 扣去玩家入场费
        int baseScore=getBaseScore();
        for(Player p:getPlayers()){
            p.setFreeMoney(p.getFreeMoney()-baseScore);
        }

        // 其次根据基数、底分、倍数，计算本局游戏货币，并存入list返回客户端渲染resultTable
        List<Map<String,Object>> resultList=new ArrayList<>();
        
        int earning=getBaseScore()*getCardinality()*getMultiple();
        for(Player p:winnerList){
            Map<String,Object> result=new HashMap<>();
            // 更新player
            long actualEarn=p.isBoss()?earning*2:earning;
            p.setFreeMoney(p.getFreeMoney()+actualEarn);
            // 结果存入
            result.put("playerID", p.getId());
            result.put("nickName", p.getNickName());
            result.put("baseScore", getBaseScore());
            result.put("multiple", getMultiple());
            result.put("earning", actualEarn);
            result.put("win", true);
            resultList.add(result);
        }
        for(Player p:loserList){
            Map<String,Object> result=new HashMap<>();
            long actualEarn=p.isBoss()?earning*2:earning;
            p.setFreeMoney(p.getFreeMoney()-actualEarn);
            result.put("playerID", p.getId());
            result.put("nickName", p.getNickName());
            result.put("baseScore", getBaseScore());
            result.put("multiple", getMultiple());
            result.put("earning", -actualEarn);
            result.put("win", false);
            resultList.add(result);
        }
        Map<String,Object> result=new HashMap<>();
        result.put("resultTable", resultList);
        result.put("players", getPlayers());
        return result;
    }
    
}
