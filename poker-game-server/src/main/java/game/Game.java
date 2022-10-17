package game;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import game.entity.Player;
import game.entity.Poker;
import game.enums.GameStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 游戏抽象层
 */
@Data
@NoArgsConstructor
public abstract class Game implements Serializable {
    
    private static final long serialVersionUID=1L;

    private Collection<Poker> pokerCollector;
    private Collection<Poker> pokerBossCollector;
    private List<Player> players=new CopyOnWriteArrayList<>();
    private GameStatusEnum status=GameStatusEnum.READY;
    private boolean handOut=false;
    private boolean over=false;

    public abstract void init();
    protected abstract void handOutPokers();
    public abstract Player getBossInstantly();

    public void addPlayer(Player player){
        players.add(player);
    }

}
