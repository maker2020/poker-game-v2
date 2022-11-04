package com.samay.game.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.samay.game.entity.Poker;
import com.samay.game.entity.Poker.PokerComparator;

/**
 * 扑克牌工具类
 * <p>
 * 包含方法：如排序。
 */
public class PokerUtil {

    private static PokerComparator comparator = Poker.PokerComparator.getPokerComparator();

    public static void sort(List<Poker> list) {
        Collections.sort(list, comparator);
    }

    /**
     * 适合打出的牌(putPokers)的排序规则
     * @param list
     */
    public static void sortForPUT(List<Poker> list) {
        if(list==null) return;
        // 记录各牌(的值)出现次数
        Map<String, Integer> countMap = new HashMap<>();
        for (Poker p : list) {
            if (!countMap.containsKey(p.getValueEnum().getValue())) {
                countMap.put(p.getValueEnum().getValue(), 1);
            } else {
                countMap.put(p.getValueEnum().getValue(), countMap.get(p.getValueEnum().getValue()) + 1);
            }
        }
        Collections.sort(list, (p1, p2) -> {
            if (countMap.get(p1.getValueEnum().getValue()) > countMap.get(p2.getValueEnum().getValue())) {
                return -1;
            } else if (countMap.get(p1.getValueEnum().getValue()) < countMap.get(p2.getValueEnum().getValue())) {
                return 1;
            } else {
                if (p1.getValueEnum().getWeight() > p2.getValueEnum().getWeight())
                    return 1;
                else if (p1.getValueEnum().getWeight() < p2.getValueEnum().getWeight())
                    return -1;
                else
                    return 0;
            }
        });
    }

}
