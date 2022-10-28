package com.samay.common.vo;

import java.util.HashMap;
import java.util.Map;

import com.samay.common.enums.ResultEnum;

/**
 * Web控制器层VO
 */
public class ResultVO {
    
    /**
     * 返回失败的结果
     * @param resultEnum
     * @return
     */
    public static Map<String,Object> fail(ResultEnum resultEnum){
        Map<String,Object> result=new HashMap<>();
        result.put("code", resultEnum.getCode());
        result.put("message", resultEnum.getMessage());
        result.put("success", false);
        return result;
    }

}
