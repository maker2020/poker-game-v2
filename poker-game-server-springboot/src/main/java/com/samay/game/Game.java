package com.samay.game;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.samay.game.entity.Player;
import com.samay.game.entity.Poker;
import com.samay.game.enums.ActionEnum;
import com.samay.game.enums.GameStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 游戏抽象层<p>
 * 该类以下字段/方法、保留的v1版本辅助变量/方法。
 * <ul>
 *  <li>handOut</li>
 *  <li>init()</li>
 * </ul>
 */
@Data
@NoArgsConstructor
public abstract class Game implements Serializable {
    
    private static final long serialVersionUID=1L;

    private transient Collection<Poker> pokerCollector;
    private transient Collection<Poker> pokerBossCollector;
    private transient List<Player> players=new CopyOnWriteArrayList<>();
    private GameStatusEnum status=GameStatusEnum.READY;
    
    /**
     * 上一个打出的牌
     */
    private Collection<Poker> lastPutPokers;
    /**
     * 上一个打出的牌的玩家id
     */
    private String lastPlayerID;
    /**
     * 正在操作的玩家id<p>
     * (本来不需要该状态变量，但这种情况: 客户端恶意请求，若没有该状态变量维护，任何人任何时刻都可以出牌)
     */
    private String actingPlayer;
    /**
     * 当前正操作的类型
     */
    private ActionEnum currentAction;
    /**
     * 地主三只牌(应该和pokerBossCollector同一个引用指向)
     */
    private Collection<Poker> bossPokers;

    /**
     * 底分
     */
    private int baseScore;
    /**
     * 倍数
     */
    private int multiple;
    /**
     * 基数
     */
    private int cardinality;

    /**
     * 地主请求轮询的序号
     */
    private transient AtomicInteger turnCallIndex=new AtomicInteger(0);
    

    // 以下变量暂时保留
    // private boolean handOut=false;
    // private boolean over=false;

    /**
     * 初始化
     */
    public abstract void init();
    
    /**
     * 分发手牌
     */
    protected abstract void handOutPokers();
    
    /**
     * 尝试得出地主
     * @return
     */
    public abstract Player getBossInstantly();

    /**
     * 游戏结算
     */
    public abstract Map<String,Object> settlement();

    /**
     * 重置游戏状态/初始化游戏状态
     */
    public abstract void restart();

    public void addPlayer(Player player){
        players.add(player);
    }

}
