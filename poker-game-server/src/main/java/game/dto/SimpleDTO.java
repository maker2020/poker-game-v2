package game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 抽象传输对象<p>
 * 包含规定字段action和tendency
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleDTO {
    
    private String action;
    private String tendency;

}
