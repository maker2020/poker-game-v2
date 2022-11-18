package com.samay.game.enums;

/**
 * 动作枚举: 对应(客户端/前端)不同的(按钮/行为/动作)
 */
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
