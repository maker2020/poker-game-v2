package com.samay.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TipPokerDTO extends SimpleDTO{
    /**
     * 提示次数，前端传，后端取余
     */
    private int tipIndex;
}
