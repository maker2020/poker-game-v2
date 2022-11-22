package com.samay.game.enums;

/**
 * 游戏阶段/状态 枚举: <p>
 * READY、RAISE、START等(同游戏从前往后的顺序)
 */
public enum GameStatusEnum {
    
    /**
     * 准备阶段
     */
    READY("ready",0),
    /**
     * 叫抢地主阶段
     */
    ORDER("order",1),
    /**
     * 加注阶段
     */
    RAISE("raise",2),
    /**
     * 开始阶段
     */
    START("start",3);

    private String status;
    private int code;

    GameStatusEnum(String status,int code){
        this.status=status;
        this.code=code;
    }

    public static GameStatusEnum getByCode(int code){
        GameStatusEnum[] enums=values();
        for(GameStatusEnum res:enums){
            if(res.code==code) return res;
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }



}
