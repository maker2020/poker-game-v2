package com.samay.game.enums;

/**
 * 房间状态枚举类: <p>
 * READY、START
 */
public enum RoomStatusEnum {
    
    READY("ready",0),
    START("start",1);

    private String status;
    private int code;

    RoomStatusEnum(String status,int code){
        this.status=status;
        this.code=code;
    }

    public static RoomStatusEnum getByCode(int code){
        RoomStatusEnum[] enums=values();
        for(RoomStatusEnum res:enums){
            if(res.code==code) return res;
        }
        return null;
    }

    public String getStatus() {
        return status;
    }

    public int getCode(){
        return code;
    }

}
