package game.entity;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import game.Game;
import game.enums.RoomStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 房间类(DTO)<p>
 * 完全重构了该类，包含游戏，用户，辅助变量等基础属性。<p>
 * 而以往的设计比较复杂，包含很多共享状态变量、辅助变量。<p>
 * @since poker-game-v2
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private RoomStatusEnum status=RoomStatusEnum.READY;
    private List<Player> players=new CopyOnWriteArrayList<>();
    private Game game;

    // 辅助<叫地主逻辑>的变量
    
    /**
     * 地主请求轮询的序号
     */
    private final AtomicInteger turnCallIndex=new AtomicInteger(0);


    public void addPlayer(Player player){
        players.add(player);
    }

    /**
     * 传入一个player，获取下一个player
     * @param player
     * @return
     */
    public Player turnPlayer(Player player) throws Exception{
        int pos=-1;
        for(int i=0;i<players.size();i++){
            Player p=players.get(i);
            if(p.getName().equals(player.getName())){
                pos=i;
                break;
            }
        }
        if(pos==-1) throw new Exception();
        pos=pos==players.size()-1?0:pos+1;
        return players.get(pos);
    }

}
