package com.openocean.arb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DEX报价
 *
 * @author lidong
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DexQuoteDTO {
    // 数量
    private BigDecimal inAmount;
    // 金额
    private BigDecimal outAmount;
}
