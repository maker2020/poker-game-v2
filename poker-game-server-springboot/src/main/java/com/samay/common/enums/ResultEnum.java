package com.samay.common.enums;

/**
 * <b>WEB常规的返回结果枚举类</b>
 * <p>
 * 根据具体需要增加
 */
public enum ResultEnum {

    SUCCESS(100, "成功"),
    FAIL(500, "失败"),
    USER_NOT_FOUND(501,"用户不存在");

    private int code;
    private String message;

    ResultEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据code返回<code>结果枚举类</code>
     * @param code
     * @return
     */
    public ResultEnum getByCode(int code){
        ResultEnum[] enums=values();
        for(ResultEnum e:enums){
            if(e.code==code) return e;
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
