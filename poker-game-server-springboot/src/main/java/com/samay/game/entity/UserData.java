package com.samay.game.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserData {
    
    private String userid;
    private long freeMoney;
    private long payMoney;
    private long winCount;
    private long loseCount;
    private long exp;

}
