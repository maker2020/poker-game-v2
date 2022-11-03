package com.samay.game;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.samay.game.entity.Player;
import com.samay.game.entity.Poker;
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

    private Collection<Poker> pokerCollector;
    private Collection<Poker> pokerBossCollector;
    private List<Player> players=new CopyOnWriteArrayList<>();
    private GameStatusEnum status=GameStatusEnum.READY;
    // 暂时保留
    // private boolean handOut=false;
    private boolean over=false;
    /**
     * 地主请求轮询的序号
     */
    private AtomicInteger turnCallIndex=new AtomicInteger(0);

    public abstract void init();
    protected abstract void handOutPokers();
    public abstract Player getBossInstantly();
    public abstract void restart();

    public void addPlayer(Player player){
        players.add(player);
    }

}
