package com.samay.game;

import com.samay.game.entity.Player;
import com.samay.game.entity.Poker;
import com.samay.game.entity.Room;
import com.samay.game.enums.ActionEnum;
import com.samay.game.enums.GameStatusEnum;
import com.samay.game.enums.PokerColorEnum;
import com.samay.game.enums.PokerValueEnum;
import com.samay.game.rule.CommonRule;
import com.samay.game.utils.PokerUtil;
import com.samay.netty.handler.aop.test.NotificationUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 普通的游戏模式
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class NormalGame extends Game {

    private transient ScheduledExecutorService service=Executors.newSingleThreadScheduledExecutor();
    private transient ScheduledFuture<?> future;

    public NormalGame() {
        restart();
    }

    public synchronized void init() {
        if (getStatus() == GameStatusEnum.READY){
            handOutPokersTest();
            // 发完牌将状态设为 加注
            setStatus(GameStatusEnum.RAISE);
        }
    }

    /**
     * 不洗牌测试
     */
    protected void handOutPokersTest() {

        class Util {
            /**
             * 插动洗牌
             * @param groupSize 每次洗牌捆绑数
             * @param pokers 牌堆的牌
             * @param targetIndex 洗至目标位置，通常为0
             * @param seedNum 抽牌位置，通常于牌数量中间范围
             */
            public static void insertRandomWithGroup(int groupSize, List<Poker> pokers,int targetIndex,int seedNum) {
                int size=pokers.size();
                int pos=seedNum+groupSize-1<size?1:(seedNum-groupSize+1>=0?0:-1);
                if(pos==-1) return;
                int start,end;
                if(pos>0) {
                    start=seedNum;
                    end=seedNum+groupSize;
                }else {
                    start=seedNum-groupSize+1;
                    end=seedNum;
                }
                for(int i=start;i<end;i++){
                    Collections.swap(pokers, i, targetIndex);
                    targetIndex=targetIndex>pokers.size()-2?1:targetIndex+1;
                }
                Collections.reverse(pokers);
            }
        }

        if (getPokerCollector() == null)
            setPokerCollector(new LinkedList<>());
        if (getPokerBossCollector() == null)
            setPokerBossCollector(new LinkedList<>());
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 4; j++) {
                getPokerCollector().add(new Poker(PokerColorEnum.getByCode(j + 1), PokerValueEnum.getByCode(i + 1)));
            }
        }
        getPokerCollector().add(new Poker(PokerColorEnum.HEART, PokerValueEnum.King));
        getPokerCollector().add(new Poker(PokerColorEnum.SPADE, PokerValueEnum.Queen));
        List<Poker> pokerCollector = (LinkedList<Poker>) getPokerCollector();
        
        int shuffleTimes=0;
        int maxTimes=new Random(System.currentTimeMillis()).nextInt(8,13);
        int seedNum=new Random(System.currentTimeMillis()).nextInt(17,34); // 这个参数专门从中间抽
        int[] randomTargetIndex=new int[]{1,2,2,2,6,6,8,8,8,12,12,12,17,20,24,28,32,36,36,39,39,40,41,41,41,41,42,42,42,42,42,42,44,47,47,47,49,49,49,52}; // 插入概率分布
        while (shuffleTimes<maxTimes) { // 共插入洗动N次
            Random random=new Random(System.currentTimeMillis());
            int groupSize=random.nextInt(3,8); // 每次a-b张捆绑洗动
            seedNum=seedNum+1>53?1:seedNum+1;
            int targetIndex=randomTargetIndex[random.nextInt(0, randomTargetIndex.length)];
            Util.insertRandomWithGroup(groupSize, pokerCollector, targetIndex,seedNum);  
            shuffleTimes++;  
        }

        int handIndex = 0;
        for (Poker poker : pokerCollector) {
            if (handIndex < 17)
                getPlayers().get(0).addPoker(poker);
            else if(handIndex >= 17 && handIndex<34)
                getPlayers().get(1).addPoker(poker);
            else if(handIndex>=34 && handIndex<51)
                getPlayers().get(2).addPoker(poker);
            else
                getPokerBossCollector().add(poker);
            handIndex++;
        }

        PokerUtil.sortForPUT((LinkedList<Poker>) getPokerBossCollector());
        // 同花顺或大小王加倍
        if (CommonRule.flush(getPokerBossCollector())) {
            setMultiple(getMultiple() * 2);
        }

        for (Player p : getPlayers()) {
            PokerUtil.sort(p.getPokers());
        }
    }

    /**
     * if return null,it means that over one player called Boss
     * thread safe,the field {@code count} is shared.
     * <p>
     * <p>
     * 这里v2版本逻辑修改了，将player的refuse加入了判断逻辑中
     * 
     * @return
     */
    public Player getBossInstantly() {
        // 直接通过变量来拿
        for(Player p:getPlayers()){
            if(p.isBoss()) return p;
        }
        // 其他方式
        if (getTurnCallIndex().get() < 3)
            return null;
        Player player = null;
        int most = 0;
        for (Player p : getPlayers()) {
            if (p.isRefuseBoss()) {
                most = Math.max(most, 0);
            } else {
                most = Math.max(most, p.getReqTimes());
            }
        }
        // 栈内不共享，因此不担心原子操作和线程安全
        int count = 0;
        for (Player p : getPlayers()) {
            if (most == p.getReqTimes() && !p.isRefuseBoss()) {
                count++;
                player = p;
            }
            if (count > 1)
                player = null;
        }
        if (player != null)
            player.setBoss(true);
        else {
            if (getTurnCallIndex().get() > 3) { // 意味着最后决定已经做出，可以得出地主
                int resIndex = 0;
                for (Player p : getPlayers()) {
                    if (p.isFirstCall()) {
                        // 只有第一个叫的人才可以进入该判断，地主是该玩家的上一个人
                        resIndex = p.getReqIndex() - 1;
                    }
                }
                for (Player p : getPlayers()) {
                    if (p.getReqIndex() == resIndex) {
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
    protected void handOutPokers() {
        if (getPokerCollector() == null)
            setPokerCollector(new LinkedList<>());
        if (getPokerBossCollector() == null)
            setPokerBossCollector(new LinkedList<>());
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 4; j++) {
                getPokerCollector().add(new Poker(PokerColorEnum.getByCode(j + 1), PokerValueEnum.getByCode(i + 1)));
            }
        }
        getPokerCollector().add(new Poker(PokerColorEnum.HEART, PokerValueEnum.King));
        getPokerCollector().add(new Poker(PokerColorEnum.SPADE, PokerValueEnum.Queen));
        // 放List打乱顺序
        List<Poker> pokerList = new ArrayList<>();
        pokerList.addAll(getPokerCollector());
        Collections.shuffle(pokerList);
        int handIndex = 0;
        for (Poker poker : pokerList) {
            if (handIndex <= 50)
                getPlayers().get(handIndex % 3).addPoker(poker);
            else
                getPokerBossCollector().add(poker);
            handIndex++;
        }

        PokerUtil.sortForPUT((LinkedList<Poker>) getPokerBossCollector());
        // 同花顺或大小王加倍
        if (CommonRule.flush(getPokerBossCollector())) {
            setMultiple(getMultiple() * 2);
        }

        for (Player p : getPlayers()) {
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
        // 玩家于游戏中的一些状态变量的重置
        for (Player p : getPlayers()) {
            p.getPokers().clear();
            p.setBoss(false);
            p.setReady(false);
            p.setRaise(false);
            p.setNotification(null);
            p.setReqTimes(0);
            p.setRefuseBoss(false);
            p.setReqIndex(0);
            p.setFirstCall(false);
            p.setDisconnected(false);
        }
        this.setStatus(GameStatusEnum.READY);
        this.setLastPutPokers(null);
        this.setLastPlayerID("");
        this.setActingPlayer("");
        this.setCurrentAction(null);
        this.setBossPokers(null);
        this.setBaseScore(200);
        this.setMultiple(2);
        this.setCardinality(2);
        this.getTurnCallIndex().set(0);

        setRemainingTime(0);
    }

    /**
     * 结算（未考虑货币不够的情况，直接为负数）<p>
     * 注意的是: 结算settlement和restart()不一样，restart是完全重置。<p>
     * settlement更注重结算展示:如表格、摊牌、ActingPlayer等需要清空的状态，
     * 并且是需要提前清除便于给客户端展示。在发送完结算数据后，会调用restart()清空状态
     */
    @Override
    public Map<String, Object> settlement(Room room) {
        NotificationUtil.showdown(this);
        setActingPlayer(null);
        setCurrentAction(null);
        List<Player> winnerList = new ArrayList<>();
        List<Player> loserList = new ArrayList<>();
        for (Player p : getPlayers()) {
            if (p.getPokers().size() == 0) {
                winnerList.add(p);
            }
        }
        if (winnerList.size() == 0)
            return null;
        Player winner = winnerList.get(0);
        if (!winner.isBoss()) {
            for (Player p : getPlayers()) {
                if (p == winner)
                    continue;
                if (!p.isBoss()) {
                    winnerList.add(p);
                } else {
                    loserList.add(p);
                }
            }
        } else {
            for (Player p : getPlayers()) {
                if (!p.isBoss()) {
                    loserList.add(p);
                }
            }
        }

        // 根据loserList判断是否为春天
        boolean spring=true;
        for(Player player:loserList){
            if(player.getPokers().size()<17){
                spring=false;
                break;
            }
        }
        if(spring){
            setMultiple(getMultiple()*2);
        }
        

        // 首先根据底分(该NormalGame是200) 扣去玩家入场费
        int baseScore = getBaseScore();
        for (Player p : getPlayers()) {
            p.setFreeMoney(p.getFreeMoney() - baseScore);
        }

        // 其次根据基数、底分、倍数，计算本局游戏货币，并存入list返回客户端渲染resultTable
        List<Map<String, Object>> resultList = new ArrayList<>();

        int earning = getBaseScore() * getCardinality() * getMultiple();
        for (Player p : winnerList) {
            Map<String, Object> result = new HashMap<>();
            // 更新player
            long actualEarn = p.isBoss() ? earning * 2 : earning;
            p.setFreeMoney(p.getFreeMoney() + actualEarn);
            p.setWinCount(p.getWinCount()+1);
            // 结果存入
            result.put("playerID", p.getId());
            result.put("nickName", p.getNickName());
            result.put("baseScore", getBaseScore());
            result.put("multiple", getMultiple());
            result.put("earning", actualEarn);
            result.put("boss", p.isBoss());
            result.put("win", true);
            resultList.add(result);
        }
        for (Player p : loserList) {
            Map<String, Object> result = new HashMap<>();
            long actualEarn = p.isBoss() ? earning * 2 : earning;
            p.setFreeMoney(p.getFreeMoney() - actualEarn);
            p.setLoseCount(p.getLoseCount()+1);
            result.put("playerID", p.getId());
            result.put("nickName", p.getNickName());
            result.put("baseScore", getBaseScore());
            result.put("multiple", getMultiple());
            result.put("earning", -actualEarn);
            result.put("boss", p.isBoss());
            result.put("win", false);
            resultList.add(result);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("resultTable", resultList);
        for(Player p:getPlayers()){
            p.getPokers().clear();
            // 这个状态为什么一起和结算更改，为了让没有继续游戏(准备)的玩家好有屏蔽其他玩家消息的状态，以此保持结算面板
            p.setReady(false);
        }
        result.put("room", room);
        result.put("game", this);
        return result;
    }

    @Override
    public void setActingPlayer(String playerID){
        super.setActingPlayer(playerID);
        NotificationUtil.setActingPlayerAfter(this);
        ActionEnum action=getCurrentAction();
        if(action!=null && playerID!=null && !"".equals(playerID)){
            long time = switch (action) {
                case ASK -> 10;
                case CALL -> 10;
                case MULTIPLE -> 5;
                case PUT -> 25;
                default -> 0;
            };
            setRemainingTime(time);
            if(future!=null && !future.isCancelled()) {
                future.cancel(true);
            }
            future=service.scheduleAtFixedRate(()->{
                setRemainingTime(getRemainingTime()-1);
            }, 0, 1, TimeUnit.SECONDS);
        }
    }

}
