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
    /**
     * 手机号
     */
    private transient String phone;
    /**
     * 头像地址
     */
    private String avatarUrl;
    /**
     * 玩家展示的昵称
     */
    private String nickName;
    /**
     * 0:未设置，1:男，2:女
     */
    private char sex='0';
    /**
     * 国家
     */
    private transient String country;
    /**
     * 省份
     */
    private transient String province;
    /**
     * 城市
     */
    private String city;
    /**
     * 语言
     */
    private transient String language;

    public User(String id,String nickName){
        this.id=id;
        this.nickName=nickName;
    }

}
