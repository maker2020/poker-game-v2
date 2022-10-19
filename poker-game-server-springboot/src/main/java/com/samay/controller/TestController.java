package com.samay.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    
    private TestBean bean;

    @Autowired
    public TestController(TestBean bean){
        this.bean=bean;
        bean.setData("additional data");
    }

    @RequestMapping("/t1")
    public String t1(){
        return bean.getData();
    }

}
