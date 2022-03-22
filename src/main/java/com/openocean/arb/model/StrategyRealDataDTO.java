package com.openocean.arb.model;

import com.openocean.arb.util.BigDecimalUtil;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;

/**
 * 策略实时数据
 *
 * @author lidong
 */
@Data
public class StrategyRealDataDTO {

    /**
     * CEX INFO
     */
    // CEX基础币种
    private String cexBaseSymbol;
    // CEX报价币种
    private String cexQuoteSymbol;
    // 交易所代码
    private String exchangeCode;


    /**
     * DEX INFO
     */
    // DEX基础币种
    private String dexBaseSymbol;
    // DEX报价币种
    private String dexQuoteSymbol;
    // DEX链代码
    private String dexChainCode;


    /**
     * PRICE
     */
    // CEX买价
    private BigDecimal cexBuyPrice;
    // CEX卖价
    private BigDecimal cexSellPrice;
    // DEX买价
    private BigDecimal dexBuyPrice;
    // DEX卖价
    private BigDecimal dexSellPrice;
    // DEX->CEX价差
    private BigDecimal dexToCexDiff;
    // CEX->DEX价差
    private BigDecimal cexToDexDiff;


    public static StrategyRealDataDTO create(StrategyParamDTO strategy) {
        StrategyRealDataDTO realDataDTO = new StrategyRealDataDTO();
        BeanUtils.copyProperties(strategy, realDataDTO);
        realDataDTO.setDexBuyPrice(BigDecimal.ZERO);
        realDataDTO.setDexSellPrice(BigDecimal.ZERO);
        realDataDTO.setCexBuyPrice(BigDecimal.ZERO);
        realDataDTO.setCexSellPrice(BigDecimal.ZERO);
        realDataDTO.setDexToCexDiff(BigDecimal.ZERO);
        realDataDTO.setCexToDexDiff(BigDecimal.ZERO);
        return realDataDTO;
    }

    public BigDecimal getCexToDexDiffPct() {
        return BigDecimalUtil.divide(cexToDexDiff, cexBuyPrice, 4);
    }

    public BigDecimal getDexToCexDiffPct() {
        return BigDecimalUtil.divide(dexToCexDiff, dexBuyPrice, 4);
    }
}
