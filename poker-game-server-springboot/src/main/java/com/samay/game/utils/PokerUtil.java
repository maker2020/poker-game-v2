package com.samay.game.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.samay.game.Game;
import com.samay.game.entity.Player;
import com.samay.game.entity.Poker;
import com.samay.game.entity.Poker.PokerComparator;
import com.samay.game.rule.CommonRule;
import com.samay.game.enums.PokerColorEnum;
import com.samay.game.enums.PokerTypeEnum;
import com.samay.game.enums.PokerValueEnum;


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

    public static List<List<Poker>> tipPokers(Player player,Game game){
        List<List<Poker>> resultList = new ArrayList<>();
        List<Poker> pokersList=player.getPokers();
        List<Poker> tmpPokersList=new ArrayList<>(){{
            for(int i=0;i<pokersList.size();i++){
                add(null);
            }
        }};
        Collections.copy(tmpPokersList, pokersList);
        Collections.reverse(tmpPokersList);
        Collection<Poker> pokers=tmpPokersList;
        //记录当前手牌各个牌的出现次数，便于进行分组统计，与上家进行比对
        Map<String, Integer> countMapSort = new HashMap<>();
        for (Poker p : pokers) {
            if (!countMapSort.containsKey(p.getValueEnum().getValue())) {
                countMapSort.put(p.getValueEnum().getValue(), 1);
            } else {
                countMapSort.put(p.getValueEnum().getValue(), countMapSort.get(p.getValueEnum().getValue()) + 1);
            }
        }
        List<Entry<String,Integer>> list=new ArrayList<>(countMapSort.entrySet());
        Collections.sort(list, ((o1, o2) -> {
            int w1=PokerValueEnum.getByValue(o1.getKey()).getWeight();
            int w2=PokerValueEnum.getByValue(o2.getKey()).getWeight();
            return w1-w2;
        }));
        LinkedHashMap<String,Integer> countMap=new LinkedHashMap<>();
        for(Entry<String,Integer> entry:list){
            countMap.put(entry.getKey(), entry.getValue());
        }

        Collection<Poker> lastPutPokers=game.getLastPutPokers();
        //获取上家出的牌的长度，确定本次提示需要出牌数量
        int size = lastPutPokers.size();
        //判断上家出牌类型，确定本次提示需要出牌的类型
        CommonRule ruleOld=new CommonRule(lastPutPokers, null);
        //当上家出牌为炸弹时，判断自己是否有比他大的炸弹或者王炸
        if(ruleOld.getPokersType() == PokerTypeEnum.BOOM && size == 4 && pokers.size() > 1){
            //记录大小王个数，若count为2，则表示当前手牌有王炸并加入提示列表中
            int count = 0;
            Iterator<String> it=countMap.keySet().iterator();
            while (it.hasNext()) {
                String value=it.next();
                if(value.equals("X") || value.equals("Y")) count++;
                if(countMap.get(value)==4){
                    if(PokerValueEnum.getByValue(value).getWeight() > lastPutPokers.iterator().next().getValueEnum().getWeight()){
                        List<Poker> selected=new ArrayList<>();
                        for(Poker p : pokers){
                            if(p.getValueEnum().getValue().equals(value)){
                                selected.add(p);
                            }
                        }
                        resultList.add(selected);
                    }
                }
                if(count == 2){
                    List<Poker> jokerPokers = new ArrayList<>(){{
                        add(new Poker(PokerColorEnum.HEART,PokerValueEnum.King));
                        add(new Poker(PokerColorEnum.SPADE,PokerValueEnum.Queen));
                    }};
                    resultList.add(jokerPokers);
                }
            }
        }
        //当上家出牌为单牌时，判断自己是否有比他大的单牌或者炸弹或者王炸
        if(ruleOld.getPokersType() == PokerTypeEnum.SINGLE){
            //判断单牌
            Iterator<String> it=countMap.keySet().iterator();
            while(it.hasNext()){
                String value = it.next();
                if(PokerValueEnum.getByValue(value).getWeight() > lastPutPokers.iterator().next().getValueEnum().getWeight()){
                    List<Poker> selected = new ArrayList<>();
                    //有多张相同单牌时，只提示第一张
                    for(Poker p : pokers){
                        if(p.getValueEnum().getValue().equals(value)){
                            selected.add(p);
                            break;
                        }
                    }
                    resultList.add(selected);
                }
            }
            //判断炸弹
            List<List<Poker>> boomOrJokerBoom = BoomOrJokerBoom(player);
            if(boomOrJokerBoom != null){
                resultList.addAll(boomOrJokerBoom);
            }
        }
        //当上家出牌为对子时，判断自己是否有比他大的对子或者炸弹或者王炸
        if(ruleOld.getPokersType() == PokerTypeEnum.DOUBLE && pokers.size() >= size){
            //判断对子
            Iterator<String> it = countMap.keySet().iterator();
            while(it.hasNext()){
                String value = it.next();
                if(countMap.get(value) > 1){
                    if(PokerValueEnum.getByValue(value).getWeight() > lastPutPokers.iterator().next().getValueEnum().getWeight()){
                        List<Poker> selected = new ArrayList<>();
                        int count = 0;
                        for(Poker p : pokers){
                            //大于等于2张时，只取前面两张
                            if(count < 2){
                                if(p.getValueEnum().getValue().equals(value)){
                                    selected.add(p);
                                    count++;
                                }
                            }else break;
                        }
                        resultList.add(selected);
                    }

                }
            }
            //判断炸弹
            List<List<Poker>> boomOrJokerBoom = BoomOrJokerBoom(player);
            if(boomOrJokerBoom != null){
                resultList.addAll(boomOrJokerBoom);
            }
        }
        //当上家出牌为相同三张时,判断自己是否有比他大的三张或者炸弹或者王炸
        if(ruleOld.getPokersType() == PokerTypeEnum.TRIPLE && pokers.size() >= size){
            //判断三张
            Iterator<String> it = countMap.keySet().iterator();
            while(it.hasNext()){
                String value = it.next();
                if(countMap.get(value) > 2){
                    if(PokerValueEnum.getByValue(value).getWeight() > lastPutPokers.iterator().next().getValueEnum().getWeight()){
                        List<Poker> selected = new ArrayList<>();
                        int count = 0;
                        for(Poker p : pokers){
                            if(count < 3){
                                if(p.getValueEnum().getValue().equals(value)){
                                    selected.add(p);
                                    count++;
                                }
                            }else break;
                        }
                        resultList.add(selected);
                    }
                }
            }
            //判断炸弹
            List<List<Poker>> boomOrJokerBoom = BoomOrJokerBoom(player);
            if(boomOrJokerBoom != null){
                resultList.addAll(boomOrJokerBoom);
            }
        }
        //当上家出牌为三带一时,判断自己是否有比他大的三带一或者炸弹或者王炸
        if(ruleOld.getPokersType() == PokerTypeEnum.TRIPLE_SINGLE && pokers.size() >= size){
            //判断三带一
            Iterator<String> it = countMap.keySet().iterator();
            while(it.hasNext()){
                String value = it.next();
                if(countMap.get(value) > 2){
                    if(PokerValueEnum.getByValue(value).getWeight() > lastPutPokers.iterator().next().getValueEnum().getWeight()){
                        //三
                        List<Poker> selected = new ArrayList<>();
                        int count = 0;
                        for(Poker p : pokers){
                            if(count < 3){
                                if(p.getValueEnum().getValue().equals(value)){
                                    selected.add(p);
                                    count++;
                                }
                            }else break;
                        }
                        //一
                        Iterator<String> it1 = countMap.keySet().iterator();
                        while(it1.hasNext()){
                            String valueSingle = it1.next();
                            if(!valueSingle.equals(value)){
                                //有多张相同单牌时，只取第一张进行三带一
                                List<Poker> selected1 = new ArrayList<>(){{
                                    for(int i = 0; i < selected.size() ; i++){
                                        add(null);
                                    }
                                }};
                                Collections.copy(selected1, selected);
                                for(Poker p : pokers){
                                    if(p.getValueEnum().getValue().equals(valueSingle)){
                                        selected1.add(p);
                                        resultList.add(selected1);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //判断炸弹
            List<List<Poker>> boomOrJokerBoom = BoomOrJokerBoom(player);
            if(boomOrJokerBoom != null){
                resultList.addAll(boomOrJokerBoom);
            }
        }
        //当上家出牌为三带二时,判断自己是否有比他大的三带二或者炸弹或者王炸
        if(ruleOld.getPokersType() == PokerTypeEnum.TRIPLE_DOUBLE && pokers.size() >= size){
            //判断三带二
            Iterator<String> it = countMap.keySet().iterator();
            while(it.hasNext()){
                String value = it.next();
                if(countMap.get(value) > 2){
                    if(PokerValueEnum.getByValue(value).getWeight() > lastPutPokers.iterator().next().getValueEnum().getWeight()){
                        //三
                        List<Poker> selected = new ArrayList<>();
                        int count = 0;
                        for(Poker p : pokers){
                            if(count < 3){
                                if(p.getValueEnum().getValue().equals(value)){
                                    selected.add(p);
                                    count++;
                                }
                            }else break;
                        }
                        //二
                        Iterator<String> it1 = countMap.keySet().iterator();
                        while(it1.hasNext()){
                            String valueDouble = it1.next();
                            if(!valueDouble.equals(value) && countMap.get(valueDouble) > 1){
                                int countDouble = 0;
                                List<Poker> selected1 = new ArrayList<>(){{
                                    for(int i = 0; i < selected.size() ; i++){
                                        add(null);
                                    }
                                }};
                                Collections.copy(selected1, selected);
                                for(Poker p : pokers){
                                    //大于等于2张时，只取前面两张
                                    if(countDouble < 2){
                                        if(p.getValueEnum().getValue().equals(valueDouble)){
                                            selected1.add(p);
                                            countDouble++;
                                        }
                                    }else{
                                        resultList.add(selected1);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //判断炸弹
            List<List<Poker>> boomOrJokerBoom = BoomOrJokerBoom(player);
            if(boomOrJokerBoom != null){
                resultList.addAll(boomOrJokerBoom);
            }
        }
        //当上家出牌为飞机(不带牌)时,判断自己是否有比他大的飞机(不带牌)或者炸弹或者王炸
        if(ruleOld.getPokersType() == PokerTypeEnum.PLANE_ALONE && pokers.size() >= size){
            //判断飞机(不带牌)
            Iterator<String> it = countMap.keySet().iterator();
            while(it.hasNext()){

            }
            //判断炸弹
            List<List<Poker>> boomOrJokerBoom = BoomOrJokerBoom(player);
            if(boomOrJokerBoom != null){
                resultList.addAll(boomOrJokerBoom);
            }
        }
        //当上家出牌为飞机带单牌时,判断自己是否有比他大的飞机带单牌或者炸弹或者王炸
        if(ruleOld.getPokersType() == PokerTypeEnum.PLANE_SINGLE && pokers.size() >= size){
            //判断飞机带单牌

            //判断炸弹
            List<List<Poker>> boomOrJokerBoom = BoomOrJokerBoom(player);
            if(boomOrJokerBoom != null){
                resultList.addAll(boomOrJokerBoom);
            }
        }
        //当上家出牌为飞机带一对时,判断自己是否有比他大的飞机带一对或者炸弹或者王炸
        if(ruleOld.getPokersType() == PokerTypeEnum.PLANE_DOUBLE && pokers.size() >= size){
            //判断飞机带一对

            //判断炸弹
            List<List<Poker>> boomOrJokerBoom = BoomOrJokerBoom(player);
            if(boomOrJokerBoom != null){
                resultList.addAll(boomOrJokerBoom);
            }
        }
        //当上家出牌为四带两张时,判断自己是否有比他大的四带两张或者炸弹或者王炸
        if(ruleOld.getPokersType() == PokerTypeEnum.BOOM_SINGLE && pokers.size() >= size){
            //判断四带两张
            Iterator<String> it=countMap.keySet().iterator();
            while(it.hasNext()){
                String value = it.next();
                if(countMap.get(value) == 4){
                    if(PokerValueEnum.getByValue(value).getWeight() > lastPutPokers.iterator().next().getValueEnum().getWeight()){
                        //四
                        List<Poker> selected = new ArrayList<>();
                        for(Poker p : pokers){
                            if(p.getValueEnum().getValue().equals(value)){
                                selected.add(p);
                            }
                        }
                        //两张
                        int count = 0;
                        for(Poker p : pokers){
                            if(count < 2){
                                if(!p.getValueEnum().getValue().equals(value)){
                                    selected.add(p);
                                }
                            }else break;
                        }
                        resultList.add(selected);
                    }
                }
            }
            //判断炸弹
            List<List<Poker>> boomOrJokerBoom = BoomOrJokerBoom(player);
            if(boomOrJokerBoom != null){
                resultList.addAll(boomOrJokerBoom);
            }
        }
        //当上家出牌为四带两对时,判断自己是否有比他大的四带两对或者炸弹或者王炸
        if(ruleOld.getPokersType() == PokerTypeEnum.BOOM_DOUBLE && pokers.size() >= size){
            //判断四带两对
            Iterator<String> it=countMap.keySet().iterator();
            while(it.hasNext()){
                String value = it.next();
                if(countMap.get(value) == 4){
                    if(PokerValueEnum.getByValue(value).getWeight() > lastPutPokers.iterator().next().getValueEnum().getWeight()){
                        //四
                        List<Poker> selected = new ArrayList<>();
                        for(Poker p : pokers){
                            if(p.getValueEnum().getValue().equals(value)){
                                selected.add(p);
                            }
                        }
                        //两对
                        Iterator<String> it1 = countMap.keySet().iterator();
                        while(it1.hasNext()){
                            
                        }
                    }
                }
            }
            //判断炸弹
            List<List<Poker>> boomOrJokerBoom = BoomOrJokerBoom(player);
            if(boomOrJokerBoom != null){
                resultList.addAll(boomOrJokerBoom);
            }
        }
        //当上家出牌为顺子时,判断自己是否有比他大的顺子或者炸弹或者王炸
        if(ruleOld.getPokersType() == PokerTypeEnum.STRAIGHTS_SINGLE && pokers.size() >= size){
            //判断顺子
            Iterator<String> it = countMap.keySet().iterator();
            List<String> valueList = new ArrayList<>();
            while(it.hasNext()){
                valueList.add(it.next());
            }
            Iterator<String> it1 = countMap.keySet().iterator();
            int count = -1;
            while(it1.hasNext()){
                String value = it1.next();
                count++;
                if(PokerValueEnum.getByValue(value).getWeight() > lastPutPokers.iterator().next().getValueEnum().getWeight()){
                    //判断是否存在顺子，根据上家的顺子的size进行寻找，若头尾都符合，则判断为顺子存在
                    if(count + size - 1 < valueList.size()){
                        if(PokerValueEnum.getByValue(valueList.get(count + size - 1)).getWeight() - PokerValueEnum.getByValue(value).getWeight() == size - 1 ){
                            List<Poker> selected = new ArrayList<>();
                            for(int i = count; i <= count + size - 1 ; i++){
                                String single = valueList.get(i);
                                for(Poker p : pokers){
                                    //找到需要的单牌，若有多张则只取第一张
                                    if(p.getValueEnum().getValue().equals(single)){
                                        selected.add(p);
                                        break;
                                    }
                                }
                            }
                            resultList.add(selected);
                        }
                    }
                    }
                    
            }
            lastPutPokers.iterator().next().getValueEnum().getWeight();

            //判断炸弹
            List<List<Poker>> boomOrJokerBoom = BoomOrJokerBoom(player);
            if(boomOrJokerBoom != null){
                resultList.addAll(boomOrJokerBoom);
            }
        }
        //当上家出牌为连对时,判断自己是否有比他大的连对或者炸弹或者王炸
        if(ruleOld.getPokersType() == PokerTypeEnum.STRAIGHTS_DOUBLE && pokers.size() >= size){
            //判断连对
            
            //判断炸弹
            List<List<Poker>> boomOrJokerBoom = BoomOrJokerBoom(player);
            if(boomOrJokerBoom != null){
                resultList.addAll(boomOrJokerBoom);
            }
        }
        return resultList;
    }

    private static List<List<Poker>> BoomOrJokerBoom(Player player){
        List<List<Poker>> resultList = new ArrayList<>();
        List<Poker> pokersList=player.getPokers();
        List<Poker> tmpPokersList=new ArrayList<>(){{
            for(int i=0;i<pokersList.size();i++){
                add(null);
            }
        }};
        Collections.copy(tmpPokersList, pokersList);
        Collections.reverse(tmpPokersList);
        Collection<Poker> pokers=tmpPokersList;
        if(pokers.size() > 1){
            //记录当前手牌各个牌的出现次数，便于进行分组统计，与上家进行比对
            Map<String, Integer> countMapSort = new HashMap<>();
            for (Poker p : pokers) {
                if (!countMapSort.containsKey(p.getValueEnum().getValue())) {
                    countMapSort.put(p.getValueEnum().getValue(), 1);
                } else {
                    countMapSort.put(p.getValueEnum().getValue(), countMapSort.get(p.getValueEnum().getValue()) + 1);
                }
            }
            List<Entry<String,Integer>> list=new ArrayList<>(countMapSort.entrySet());
            Collections.sort(list, ((o1, o2) -> {
                int w1=PokerValueEnum.getByValue(o1.getKey()).getWeight();
                int w2=PokerValueEnum.getByValue(o2.getKey()).getWeight();
                return w1-w2;
            }));
            LinkedHashMap<String,Integer> countMap=new LinkedHashMap<>();
            for(Entry<String,Integer> entry:list){
                countMap.put(entry.getKey(), entry.getValue());
            }
                Iterator<String> it=countMap.keySet().iterator();
                //记录大小王个数，若count为2，则表示当前手牌有王炸并加入提示列表中
                int count = 0;
                //在List中写入炸弹和王炸
                while(it.hasNext()){
                    String value = it.next();
                    if(value.equals("X") || value.equals("Y")) count++;
                    if(countMap.get(value)==4){
                        List<Poker> selected=new ArrayList<>();
                            for(Poker p : pokers){
                                if(p.getValueEnum().getValue().equals(value)){
                                selected.add(p);
                            }
                        }
                    resultList.add(selected);
                }
                    if(count == 2){
                        List<Poker> jokerPokers = new ArrayList<>(){{
                            add(new Poker(PokerColorEnum.HEART,PokerValueEnum.King));
                            add(new Poker(PokerColorEnum.SPADE,PokerValueEnum.Queen));
                        }};
                        resultList.add(jokerPokers);
                    }
                }
        }
        return resultList;
    }

}
