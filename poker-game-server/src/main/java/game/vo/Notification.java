package game.vo;

import game.enums.ActionEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification{
    
    private ActionEnum type;
    private boolean choice;
    /**
     * user唯一标识(可以是username、id等)
     */
    private String user;

}