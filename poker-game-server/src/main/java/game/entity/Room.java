package game.entity;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import game.Game;
import game.enums.RoomStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 房间类(DTO)<p>
 * 完全重构了该类，包含游戏，用户等基础属性。<p>
 * 而以往的设计比较复杂，包含很多共享状态变量。
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

    public void addPlayer(Player player){
        players.add(player);
    }

}
