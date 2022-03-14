package com.openocean.arb.bot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单薄条目
 *
 * @author lidong
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderBookItem {
    // 价格
    private BigDecimal price;
    // 数量
    private BigDecimal qty;
}
