package com.samay.controller;

import org.springframework.stereotype.Component;

@Component
public class TestBean {
    
    private String data="";

    public TestBean(){
        System.out.println("testBean constructor");
    }

    public TestBean(String data){
        System.out.println("testBean constructor(data)");
        this.data=data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        System.out.println("testBean setData");
        this.data = data;
    }

}
