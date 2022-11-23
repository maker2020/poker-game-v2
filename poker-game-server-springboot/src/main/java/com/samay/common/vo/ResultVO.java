package com.samay.common.vo;

import java.util.HashMap;
import java.util.Map;

import com.samay.common.enums.ResultEnum;

/**
 * Web服务返回对象(VO)的常用模板
 */
public class ResultVO {
    
    /**
     * 返回结果
     * @param resultEnum
     * @return
     */
    public static Map<String,Object> fail(ResultEnum resultEnum){
        Map<String,Object> result=new HashMap<>();
        result.put("code", resultEnum.getCode());
        result.put("message", resultEnum.getMessage());
        return result;
    }

}
