package com.samay.game.enums;

/**
 * 扑克花色枚举
 */
public enum PokerColorEnum {
    /**
     * 方片
     */
    SQUARE("♦",1),
    /**
     * 红桃
     */
    HEART("♥",2),
    /**
     * 黑桃
     */
    SPADE("♠",3),
    /**
     * 梅花
     */
    CLUB("♣",4);

    private String color;
    private int code;

    PokerColorEnum(String color,int code){
        this.color=color;
        this.code=code;
    }

    public static PokerColorEnum getByColor(String color){
        PokerColorEnum[] enums=values();
        for(PokerColorEnum res:enums){
            if(res.color.equals(color)) return res;
        }
        return null;
    }

    public static PokerColorEnum getByCode(int code){
        PokerColorEnum[] enums=values();
        for(PokerColorEnum res:enums){
            if(res.code==code) return res;
        }
        return null;
    }

    public String getColor() {
        return color;
    }

    public int getCode(){
        return code;
    }

    @Override
    public String toString(){
        return color;
    }

}
