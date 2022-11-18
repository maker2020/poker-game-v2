package com.samay.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.samay.game.entity.Player;
import com.samay.game.entity.Poker;
import com.samay.game.enums.GameStatusEnum;
import com.samay.game.enums.PokerColorEnum;
import com.samay.game.enums.PokerValueEnum;
import com.samay.game.utils.PokerUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 娱乐的游戏模式
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@ToString
@Deprecated
public class HappyGame extends Game{

    public HappyGame(List<Player> players){
        super.setPlayers(players);
    }

    public synchronized void init(){
        if(getStatus()==GameStatusEnum.START) return;
        if(getStatus()!=GameStatusEnum.START) handOutPokers();
        setStatus(GameStatusEnum.START);
    }

    /**
     * if return null,it means that over one player called Boss
     * thread safe,the field {@code count} is shared.
     * @return
     */
    public Player getBossInstantly(){
        Player player=null;
        int most=0;
        for(Player p:getPlayers()){
            most=Math.max(most, p.getReqTimes());
        }
        // 栈内不共享，因此不担心原子操作和线程安全
        int count=0;
        for(Player p:getPlayers()){
            if(most==p.getReqTimes()) {
                count++;
                player=p;
            }
            if(count>1) return null;
        }
        return player;
    }

    /**
     * HappyGame的发牌逻辑：......
     */
    protected void handOutPokers(){
        setPokerCollector(new ArrayList<>());
        setPokerBossCollector(new ArrayList<>());
        for(int i=0;i<13;i++){
            for(int j=0;j<4;j++){
                getPokerCollector().add(new Poker(PokerColorEnum.SPADE, PokerValueEnum.A));
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
        for(Player p:getPlayers()){
            PokerUtil.sort(p.getPokers());
        }
    }

    @Override
    public void restart() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Map<String,Object> settlement() {
        return null;        
    }

    
}
