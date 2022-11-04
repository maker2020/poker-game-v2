package com.samay.game.enums;

/**
 * 牌组类型枚举
 */
public enum PokerTypeEnum {
    
    BOOM(0),
    SINGLE(1),
    DOUBLE(2),
    TRIPLE(3),
    TRIPLE_SINGLE(4),
    TRIPLE_DOUBLE(5),
    PLANE_ALONE(6),
    PLANE_SINGLE(7),
    PLANE_DOUBLE(8),
    BOOM_SINGLE(9),
    BOOM_DOUBLE(10),
    STRAIGHTS_SINGLE(11),
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
