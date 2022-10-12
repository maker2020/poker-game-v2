package game.utils;

import java.util.Collections;
import java.util.List;

import game.entity.Poker;
import game.entity.Poker.PokerComparator;

/**
 * 扑克牌工具类<p>
 * 包含方法：如排序。
 */
public class PokerUtil {
    
    private static PokerComparator comparator=Poker.PokerComparator.getPokerComparator();
    
    public static void sort(List<Poker> list){
        Collections.sort(list, comparator);
    }

}
