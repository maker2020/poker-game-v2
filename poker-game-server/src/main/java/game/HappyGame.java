package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import game.entity.Player;
import game.entity.Poker;
import game.enums.PokerColorEnum;
import game.enums.PokerValueEnum;
import game.utils.PokerUtil;
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
public class HappyGame extends Game{

    public HappyGame(List<Player> players){
        super.setPlayers(players);
    }

    public synchronized void init(){
        if(!isHandOut()) handOutPokers();
        setHandOut(true);
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
                getPokerCollector().add(new Poker(PokerColorEnum.BLACK_HEART, PokerValueEnum.A));
            }    
        }
        getPokerCollector().add(new Poker(PokerColorEnum.RED_HEART, PokerValueEnum.King));
        getPokerCollector().add(new Poker(PokerColorEnum.BLACK_HEART, PokerValueEnum.Queen));
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

    
}
