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
    /**
     * 玩家展示的昵称
     */
    private String nickName;
    /**
     * 0:未设置，1:男，2:女
     */
    private char sex='1';
    private long freeMoney;
    private long payMoney;
    private long winCount;
    private long loseCount;
    private long exp;

}
