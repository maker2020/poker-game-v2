package com.samay.game.enums;

/**
 * 动作枚举: 对应(客户端/前端)不同的(按钮/行为/动作)<p>
 * 
 * <b>重点</b>: 每一部分action都有它们共同的enum，如DOUBLE和NO_DOUBLE的公共就是MULTIPLE,
 * 为什么要这样，用于在请求中区分哪一类Handler去处理
 */
public enum ActionEnum {
    
    READY("ready"),

    // Req地主相关
    CALL("call"),
    ASK("ask"),
    
    // 出牌相关
    PUT("put"),

    // 提示相关
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
