package com.samay.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.samay.game.entity.Poker;
import com.samay.game.enums.PokerColorEnum;
import com.samay.game.enums.PokerValueEnum;
import com.samay.service.ProducerService;

@RestController
@RequestMapping("/producer")
public class ProducerController {
    
    @Autowired
    private ProducerService producerService;
    
    @RequestMapping("/send")
    public void send(){
        producerService.sendTestJson(new Poker(PokerColorEnum.CLUB,PokerValueEnum.A));
    }

}
