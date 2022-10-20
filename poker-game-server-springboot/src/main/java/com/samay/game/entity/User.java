package com.samay.game.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entity(user)<p>
 * 小程序用户信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
// record User
@EqualsAndHashCode(of = "id")
@ToString
public class User {
    
    private String id;
    private String phone;
    private String nickName;
    private char sex;
    private long freeMoney;
    private long payMoney;
    private long winCount;
    private long loseCount;
    private long exp;

}
