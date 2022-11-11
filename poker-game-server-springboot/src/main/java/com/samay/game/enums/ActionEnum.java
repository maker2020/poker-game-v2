package com.samay.game.enums;

public enum ActionEnum {
    
    READY("ready"),
    CALL("call"),
    ASK("ask"),
    PUT("put"),
    TIP("tip"),

    // 加注相关
    MULTIPLE("multiple"),
    DOUBLE("double"),
    DOUBLE_PLUS("doublePlus"),
    NO_DOUBLE("noDouble");

    private String action;

    ActionEnum(String action){
        this.action=action;
    }

    public String getAction() {
        return action;
    }

}
