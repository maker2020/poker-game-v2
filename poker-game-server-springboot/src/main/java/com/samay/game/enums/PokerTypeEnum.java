package com.samay.game.enums;

/**
 * 牌组类型枚举
 */
public enum PokerTypeEnum {
    
    /**
     * 炸弹
     */
    BOOM(0),
    /**
     * 单牌
     */
    SINGLE(1),
    /**
     * 对子
     */
    DOUBLE(2),
    /**
     * 三只
     */
    TRIPLE(3),
    /**
     * 三带一
     */
    TRIPLE_SINGLE(4),
    /**
     * 三带一对
     */
    TRIPLE_DOUBLE(5),
    /**
     * 飞机不带翅膀
     */
    PLANE(6),
    /**
     * 飞机带两个单牌
     */
    PLANE_SINGLE(7),
    /**
     * 飞机带两个对子
     */
    PLANE_DOUBLE(8),
    /**
     * 炸弹带两个单牌
     */
    BOOM_SINGLE(9),
    /**
     * 炸弹带两个对子
     */
    BOOM_DOUBLE(10),
    /**
     * 顺子
     */
    STRAIGHTS_SINGLE(11),
    /**
     * 连对
     */
    STRAIGHTS_DOUBLE(12),
    /**
     * 同花顺
     */
    FLUSH(13);

    private int code;

    PokerTypeEnum(int code){
        this.code=code;
    }

    public static PokerTypeEnum getByCode(int code){
        PokerTypeEnum[] enums=values();
        for(PokerTypeEnum res:enums){
            if(res.code==code) return res;
        }
        return null;
    }

    public int getCode() {
        return code;
    }

}
