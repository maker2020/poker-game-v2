package game.entity;

import game.enums.PokerColorEnum;
import game.enums.PokerValueEnum;
import java.io.Serializable;
import java.util.Comparator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 扑克牌类(DTO)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Poker implements Serializable{
    private PokerColorEnum colorEnum;
    private PokerValueEnum valueEnum;

    /**
     * 单例模式的扑克牌比较器，只能通过PokerUtil.getComparator获取唯一实例。
     */
    public static class PokerComparator implements Comparator<Poker>{
        
        private static volatile PokerComparator pokerComparator;

        private PokerComparator(){}

        public static PokerComparator getPokerComparator(){
            if(pokerComparator==null){
                synchronized(PokerComparator.class){
                    if(pokerComparator==null){
                        pokerComparator=new PokerComparator();
                    }
                }
            }
            return pokerComparator;
        }

        @Override
        public int compare(Poker o1, Poker o2) {
            if(o1.getValueEnum().getWeight()>o2.getValueEnum().getWeight()) return -1;
            if(o1.getValueEnum().getWeight()<o2.getValueEnum().getWeight()) return 1;
            return 0;
        }
    }

    /**
     * 重写Poker的equals目的为了Map使用，但业务逻辑局限导致hash冲突，暂无可避免。
     */
    @Override
    public boolean equals(Object p){
        if(p instanceof Poker poker){
            return poker.valueEnum==this.valueEnum;
        }
        return false;
    }

    @Override
    public int hashCode(){
        return valueEnum.getCode();
    }

    @Override
    public String toString(){
        return colorEnum.toString()+valueEnum.toString();
    }
}
