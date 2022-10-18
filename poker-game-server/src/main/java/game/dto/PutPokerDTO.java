package game.dto;

import java.util.List;

import game.entity.Poker;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 出牌阶段DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PutPokerDTO extends SimpleDTO{
    
    private List<Poker> putPokers;

}
