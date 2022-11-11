package com.samay.game.enums;

public enum GameStatusEnum {
    
    READY("ready",0),
    RAISE("raise",1),
    START("start",2);

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
