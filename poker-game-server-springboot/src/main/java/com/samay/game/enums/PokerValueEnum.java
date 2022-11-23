package com.samay.game.enums;

/**
 * 扑克牌值枚举
 */
public enum PokerValueEnum {
    A("A",1,12),
    Two("2",2,14),
    Three("3",3,1),
    Four("4",4,2),
    Five("5",5,3),
    Six("6",6,4),
    Seven("7",7,5),
    Eight("8",8,6),
    Nine("9",9,7),
    Ten("10",10,8),
    J("J",11,9),
    Q("Q",12,10),
    K("K",13,11),
    Queen("X",14,15),
    King("Y",15,16),
    
    BLANK("*",-1,-1);

    private String value;
    private int code;
    private int weight;

    PokerValueEnum(String value,int code,int weight){
        this.value=value;
        this.code=code;
        this.weight=weight;
    }

    public static PokerValueEnum getByValue(String value){
        PokerValueEnum[] enums=values();
        for(PokerValueEnum res:enums){
            if(res.value.equals(value)) return res;
        }
        return null;
    }

    public static PokerValueEnum getByCode(int code){
        PokerValueEnum[] enums=values();
        for(PokerValueEnum res:enums){
            if(res.code==code) return res;
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString(){
        return value;
    }
}
