package com.samay.controller.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.samay.common.enums.ResultEnum;
import com.samay.common.vo.ResultVO;
import com.samay.game.entity.User;
import com.samay.service.ItemService;
import com.samay.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @RequestMapping("/load")
    public Map<String,Object> load(String userID){
        User user=userService.findUserById(userID);
        if(user==null) return ResultVO.fail(ResultEnum.USER_NOT_FOUND);
        Map<String,Object> result=new HashMap<>();
        result.put("user", user);
        result.put("items", itemService.listItems(userID));
        return result;
    }

}
