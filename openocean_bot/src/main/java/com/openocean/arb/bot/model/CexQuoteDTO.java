package com.openocean.arb.bot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * CEX报价
 *
 * @author lidong
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CexQuoteDTO {
    // 数量
    private BigDecimal qty;
    // 金额
    private BigDecimal amount;
    // 限价
    private BigDecimal limitPrice;
}
