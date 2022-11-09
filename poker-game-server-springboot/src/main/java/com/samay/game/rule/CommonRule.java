package com.samay.game.rule;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.samay.game.entity.Poker;
import com.samay.game.enums.PokerColorEnum;
import com.samay.game.enums.PokerTypeEnum;
import com.samay.game.enums.PokerValueEnum;

/**
 * <b>通用斗地主规则判断</b>
 * <p>
 * 
 * PECS原则（上界仅get/生产，下界仅add/消费。)
 * <p>
 * 
 * 该类中判断后还原引用是必要的，判断会从左至右依次执行(需要原始数据)
 */
public class CommonRule implements GameRule {

    /**
     * <b>当前选中的牌(已排好序)</b><p>
     * 排序规则依次：1.从大到小 2.出现次数最多<p>
     * 基于排序，验证规则写起来更简单，可以采用硬编码即：get(1),get(2)
     */
    private Collection<? extends Poker> putPokers;
    /**
     * 需要压制的Pokers
     */
    // @SuppressWarnings("unused")
    private Collection<? extends Poker> lastPutPokers;

    /**
     * 通用判断规则，请确保传入的两个集合被sortForPUT排序过
     * 
     * @param putPokers
     * @param lastPutPokers
     */
    public CommonRule(Collection<? extends Poker> putPokers, Collection<? extends Poker> lastPutPokers) {
        this.putPokers = putPokers;
        this.lastPutPokers = lastPutPokers;
    }

    @Override
    public boolean valid() {
        return inputValid() && compareValid();
    }

    private boolean inputValid() {
        if ((putPokers == null && lastPutPokers == null))
            return false;
        else if (putPokers == null && lastPutPokers != null)
            return true;
        return single() || doublePut() || three() || threeWithOne() ||
                threeWithTwo() || planeAlone() || planeWithSingle() ||
                planeWithDouble() || fourWithTwo() || fourWithFour() ||
                singleStraights() || doubleStraights() || boom();
    }

    private boolean compareValid() {
        if (lastPutPokers == null || lastPutPokers.size() == 0 || putPokers == null || putPokers.size() == 0)
            return true;
        Collection<? extends Poker> backup = putPokers;
        putPokers = lastPutPokers;
        // 验证lastPutPokers后还原引用
        // 前卫式
        if (single()) {
            putPokers = backup;
            return bySingle();
        }
        if (doublePut()) {
            putPokers = backup;
            return byDoublePut();
        }
        if (three()) {
            putPokers = backup;
            return byThree();
        }
        if (threeWithOne()) {
            putPokers = backup;
            return byThreeWithOne();
        }
        if (threeWithTwo()) {
            putPokers = backup;
            return byThreeWithTwo();
        }
        if (planeAlone()) {
            putPokers = backup;
            return byPlaneAlone();
        }
        if (planeWithSingle()) {
            putPokers = backup;
            return byPlaneWithSingle();
        }
        if (planeWithDouble()) {
            putPokers = backup;
            return byPlaneWithDouble();
        }
        if (fourWithTwo()) {
            putPokers = backup;
            return byFourWithTwo();
        }
        if (fourWithFour()) {
            putPokers = backup;
            return byFourWithFour();
        }
        if (singleStraights()) {
            putPokers = backup;
            return bySingleStraights();
        }
        if (doubleStraights()) {
            putPokers = backup;
            return byDoubleStraights();
        }
        if (boom()) {
            putPokers = backup;
            return byBoom();
        }
        putPokers = backup;
        return true;
    }

    private boolean bySingle() {
        if (boom())
            return true;
        if (!single())
            return false;
        return putPokers.iterator().next().getValueEnum().getWeight() > lastPutPokers.iterator().next().getValueEnum()
                .getWeight();
    }

    private boolean byDoublePut() {
        if (boom())
            return true;
        if (!doublePut())
            return false;
        return putPokers.iterator().next().getValueEnum().getWeight() > lastPutPokers.iterator().next().getValueEnum()
                .getWeight();
    }

    private boolean byThree() {
        if (boom())
            return true;
        if (!three())
            return false;
        return putPokers.iterator().next().getValueEnum().getWeight() > lastPutPokers.iterator().next().getValueEnum()
                .getWeight();
    }

    private boolean byThreeWithOne() {
        if (boom())
            return true;
        if (!threeWithOne())
            return false;
        return putPokers.iterator().next().getValueEnum().getWeight() > lastPutPokers.iterator().next().getValueEnum()
                .getWeight();
    }

    private boolean byThreeWithTwo() {
        if (boom())
            return true;
        if (!threeWithTwo())
            return false;
        return putPokers.iterator().next().getValueEnum().getWeight() > lastPutPokers.iterator().next().getValueEnum()
                .getWeight();
    }

    private boolean byPlaneAlone() {
        if (boom())
            return true;
        if (!planeAlone())
            return false;
        return putPokers.iterator().next().getValueEnum().getWeight() > lastPutPokers.iterator().next().getValueEnum()
                .getWeight();
    }

    private boolean byPlaneWithSingle() {
        if (boom())
            return true;
        if (!planeWithSingle())
            return false;
        return putPokers.iterator().next().getValueEnum().getWeight() > lastPutPokers.iterator().next().getValueEnum()
                .getWeight();
    }

    private boolean byPlaneWithDouble() {
        if (boom())
            return true;
        if (!planeWithDouble())
            return false;
        return putPokers.iterator().next().getValueEnum().getWeight() > lastPutPokers.iterator().next().getValueEnum()
                .getWeight();
    }

    private boolean byFourWithTwo() {
        if (boom())
            return true;
        if (!fourWithTwo())
            return false;
        return putPokers.iterator().next().getValueEnum().getWeight() > lastPutPokers.iterator().next().getValueEnum()
                .getWeight();
    }

    private boolean byFourWithFour() {
        if (boom())
            return true;
        if (!fourWithFour())
            return false;
        return putPokers.iterator().next().getValueEnum().getWeight() > lastPutPokers.iterator().next().getValueEnum()
                .getWeight();
    }

    private boolean bySingleStraights() {
        if (boom())
            return true;
        if (!singleStraights())
            return false;
        if (lastPutPokers.size() != putPokers.size())
            return false;
        return putPokers.iterator().next().getValueEnum().getWeight() > lastPutPokers.iterator().next().getValueEnum()
                .getWeight();
    }

    private boolean byDoubleStraights() {
        if (boom())
            return true;
        if (!doubleStraights())
            return false;
        if (lastPutPokers.size() != putPokers.size())
            return false;
        return putPokers.iterator().next().getValueEnum().getWeight() > lastPutPokers.iterator().next().getValueEnum()
                .getWeight();
    }

    private boolean byBoom() {
        if (!boom())
            return false;
        return putPokers.iterator().next().getValueEnum().getWeight() > lastPutPokers.iterator().next().getValueEnum()
                .getWeight();
    }

    /**
     * single、doublePut、threeWithOne、threeWithTwo、
     * planeAlone、planeWithTwo、planeWithFour、
     * fourWithTwo、fourWithFour、
     * singleStraights、doubleStraights、
     * boom
     */

    private boolean single() {
        return putPokers.size() == 1;
    }

    private boolean doublePut() {
        if (putPokers.size() != 2)
            return false;
        Iterator<? extends Poker> it = putPokers.iterator();
        return it.next().getValueEnum().equals(it.next().getValueEnum());
    }

    private boolean three() {
        if (putPokers.size() != 3)
            return false;
        List<? extends Poker> list = (List<? extends Poker>) putPokers;
        return list.get(0).equalsValue(list.get(1)) && list.get(1).equalsValue(list.get(2));
    }

    private boolean threeWithOne() {
        if (putPokers.size() != 4)
            return false;
        List<? extends Poker> list = (List<? extends Poker>) putPokers;
        boolean f1 = list.get(0).equalsValue(list.get(1)) && list.get(1).equalsValue(list.get(2));
        boolean f2 = !list.get(2).equalsValue(list.get(3));
        return f1 && f2;
    }

    private boolean threeWithTwo() {
        if (putPokers.size() != 5)
            return false;
        List<? extends Poker> list = (List<? extends Poker>) putPokers;
        putPokers = list.subList(0, 4);
        boolean f1 = threeWithOne();
        boolean f2 = list.get(4).equalsValue(list.get(3));
        // 判断完后必须还原引用指向
        putPokers = list;
        return f1 && f2;
    }

    private boolean planeAlone() {
        if (putPokers.size() < 6)
            return false;
        Iterator<? extends Poker> it = putPokers.iterator();
        Poker last = it.next();
        int countSame = 0;
        int planeLen = 0;
        while (it.hasNext()) {
            Poker now = it.next();
            if(countSame==2){
                if(now.getValueEnum().getWeight()!=last.getValueEnum().getWeight()+1) return false;
                countSame=0;
            }
            if (now.getValueEnum() == last.getValueEnum()) {
                countSame++;
                if(countSame==2) planeLen++;
            }else{
                countSame=0;
            }
            last = now;
        }
        return planeLen*3 == putPokers.size();
    }

    private boolean planeWithSingle() {
        if (putPokers.size() < 8)
            return false;
        List<? extends Poker> list = (List<? extends Poker>) putPokers;
        int last=list.size()-2; // (最小飞机的可能)
        int planeLen=0;
        while (last>5) {
            putPokers=list.subList(0, last);
            if(planeAlone()){
                planeLen=putPokers.size();
                break;
            }else last--;
        }
        // 恢复
        putPokers=list;
        return planeLen!=0 && list.size()-last==planeLen/3;
    }

    private boolean planeWithDouble() {
        if (putPokers.size() < 10)
            return false;
        List<? extends Poker> list = (List<? extends Poker>) putPokers;
        int last=list.size()-4;
        int planeLen=0;
        while (last>5) {
            putPokers=list.subList(0, last);
            if(planeAlone()){
                planeLen=putPokers.size();
                break;
            }else last--;
        }
        boolean f1=planeLen!=0;
        // 判断带的牌皆为对子
        boolean f2=true;
        int begin=last;
        while (begin<list.size()-2) {
            putPokers=list.subList(begin, begin+2);
            if(!doublePut()){
                f2=false;
                break;
            }
            begin+=2;
        }
        // 判断对子数量满足纯飞机/3的数量
        boolean f3=planeLen/3==(list.size()-last)/2;
        putPokers = list;
        return f1&&f2&&f3;
    }

    private boolean fourWithTwo() {
        if (putPokers.size() != 6)
            return false;
        List<? extends Poker> list = (List<? extends Poker>) putPokers;
        Collection<? extends Poker> backup = putPokers;
        putPokers = list.subList(0, 4);
        boolean f1 = boom();
        putPokers = list.subList(4, 6);
        boolean f2 = !list.get(3).equalsValue(list.get(4)) && !list.get(3).equalsValue(list.get(5));
        putPokers = backup;
        return f1 && f2;
    }

    /**
     * @author 蒋能
     * @return
     */
    private boolean fourWithFour() {
        if (putPokers.size() != 8)
            return false;
        List<? extends Poker> list = (List<? extends Poker>) putPokers;
        int i = 0;
        while (i < list.size()) {
            if (i < list.size() - 4) {
                for (int a = 0; a < 3; a++) {
                    if (!list.get(a).equalsValue(list.get(a + 1)))
                        return false;
                }
                i = i + 4;
            } else if (i == 4) {
                if (!list.get(i).equalsValue(list.get(i + 1)))
                    return false;
                if (list.get(i).equalsValue(list.get(i + 2)))
                    return false;
                i = i + 2;
            } else {
                if (!list.get(i).equalsValue(list.get(i + 1)))
                    return false;
                i = i + 2;
            }
        }
        return true;
    }

    private boolean singleStraights() {
        if (putPokers.size() < 5)
            return false;
        Iterator<? extends Poker> it = putPokers.iterator();
        int lastWeight = it.next().getValueEnum().getWeight();
        while (it.hasNext()) {
            int nowWeight = it.next().getValueEnum().getWeight();
            if (lastWeight + 1 != nowWeight)
                return false;
            lastWeight = nowWeight;
        }
        return true;
    }

    /**
     * @author 蒋能
     * @return
     */
    private boolean doubleStraights() {
        if (putPokers.size() < 5 || putPokers.size() % 2 != 0)
            return false;
        List<? extends Poker> list = (List<? extends Poker>) putPokers;
        int i = 0;
        for (int j = 0; j < list.size(); j++) {
            if (list.get(j).getValueEnum().getWeight() == 13)
                return false;
        }
        while (i < list.size()) {
            if (i < list.size() - 2) {
                if (list.get(i).equalsValue(list.get(i + 1))
                        && list.get(i).getValueEnum().getWeight() + 1 == list.get(i + 2).getValueEnum().getWeight()) {
                    i = i + 2;
                } else {
                    return false;
                }
            } else if (i == list.size() - 2) {
                if (!list.get(i).equalsValue(list.get(i + 1)))
                    return false;
                i = i + 2;
            }
        }
        return true;
    }

    private boolean boom() {
        if (putPokers.size() == 2) {
            return putPokers.contains(new Poker(PokerColorEnum.SPADE, PokerValueEnum.Queen))
                    && putPokers.contains(new Poker(PokerColorEnum.HEART, PokerValueEnum.King));
        } else {
            if (putPokers.size() > 3) {
                Iterator<? extends Poker> it = putPokers.iterator();
                Poker last = it.next();
                while (it.hasNext()) {
                    Poker now = it.next();
                    if (now.getValueEnum() != last.getValueEnum()) {
                        return false;
                    }
                    last = now;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 判断pokers是否是同花顺<p>
     * 注意：同样的类似valid()方法，也需要在调用flush之前用sortForPut排序
     * @param pokers
     * @return
    ' */
    public static boolean flush(Collection<? extends Poker> pokers){
        if(pokers.size()<3) return false;
        Iterator<? extends Poker> it=pokers.iterator();
        Poker lastPoker=it.next();
        while (it.hasNext()) {
            Poker curPoker=it.next();
            if(curPoker.getValueEnum().getWeight()!=lastPoker.getValueEnum().getWeight()+1)
                return false;
            if(curPoker.getColorEnum()!=lastPoker.getColorEnum())
                return false;
        }
        return true;
    }

    /**
     * <b>返回玩家牌型</b>
     * @return
     */
    public PokerTypeEnum getPokersType(){
        if(boom()) return PokerTypeEnum.BOOM;
        if(single()) return PokerTypeEnum.SINGLE;
        if(doublePut()) return PokerTypeEnum.DOUBLE;
        if(three()) return PokerTypeEnum.TRIPLE;
        if(threeWithOne()) return PokerTypeEnum.TRIPLE_SINGLE;
        if(threeWithTwo()) return PokerTypeEnum.TRIPLE_DOUBLE;
        if(planeAlone()) return PokerTypeEnum.PLANE_ALONE;
        if(planeWithSingle()) return PokerTypeEnum.PLANE_SINGLE;
        if(planeWithDouble()) return PokerTypeEnum.PLANE_DOUBLE;
        if(fourWithTwo()) return PokerTypeEnum.BOOM_SINGLE;
        if(fourWithFour()) return PokerTypeEnum.BOOM_DOUBLE;
        if(singleStraights()) return PokerTypeEnum.STRAIGHTS_SINGLE;
        if(doubleStraights()) return PokerTypeEnum.STRAIGHTS_DOUBLE;
        return null;
    }

}