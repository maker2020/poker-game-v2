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
 * 完全重构了该类，包含游戏，用户。
 * @since poker-game-v2
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private RoomStatusEnum status;
    private List<Player> players=new CopyOnWriteArrayList<>();
    private Game game;

}
