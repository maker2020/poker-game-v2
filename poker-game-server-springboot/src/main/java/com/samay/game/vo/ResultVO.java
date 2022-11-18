package com.samay.game.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <b>Netty返回结果对象</b><p>
 * code,msg,data(其中code,msg暂没有抽象出枚举)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultVO<T> {
    
    private Integer code;
    private String msg;
    private T data;

    public ResultVO(Integer code){
        this.code=code;
    }

    public ResultVO(Integer code,String msg){
        this.code=code;
        this.msg=msg;
    }

}
