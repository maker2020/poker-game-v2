package com.samay.game.entity;

import java.io.Serializable;

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
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String id;
    private transient String phone;
    /**
     * 玩家展示的昵称
     */
    private String nickName;
    /**
     * 0:未设置，1:男，2:女
     */
    private char sex='1';
    /**
     * 游戏普通货币
     */
    private long freeMoney;
    /**
     * 游戏需重置货币
     */
    private transient long payMoney;
    /**
     * 胜场
     */
    private long winCount;
    /**
     * 败场
     */
    private long loseCount;
    /**
     * 经验(换算等级、称谓)
     */
    private long exp;

}
