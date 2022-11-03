package com.samay.game.entity;

import com.samay.game.enums.PokerColorEnum;
import com.samay.game.enums.PokerValueEnum;
import java.io.Serializable;
import java.util.Comparator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <b>扑克牌类(POJO)</b>
 * equals重写规则: color+value (对比v1不同，v1为了多种排序规则，仅将value作为equals判断要素)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"colorEnum","valueEnum"})
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

    @Override
    public String toString(){
        return colorEnum.toString()+valueEnum.toString();
    }

}
