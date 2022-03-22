package com.openocean.arb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrategyResultDTO {
    // CEX 基础币种
    private String cexBaseSymbol;
    // CEX 报价币种
    private String cexQuoteSymbol;
    // CEX买卖方向：0-买；1-卖
    private String cexDirection;
    // CEX价格
    private BigDecimal cexPrice;
    // CEX数量
    private BigDecimal cexQty;
    // CEX金额
    private BigDecimal cexAmount;

    // DEX 基础币种
    private String dexBaseSymbol;
    // DEX 报价币种
    private String dexQuoteSymbol;
    // DEX链代码
    private String dexChainCode;
    // DEX买卖方向：0-买；1-卖
    private String dexDirection;
    // DEX价格
    private BigDecimal dexPrice;
    // DEX数量
    private BigDecimal dexQty;
    // DEX金额
    private BigDecimal dexAmount;

    // 基础币种盈利
    private BigDecimal baseProfit;
    // 报价币种盈利
    private BigDecimal quoteProfit;
    // gas费
    private BigDecimal gasFee;

}
