package com.openocean.arb.bot.model;

import com.openocean.arb.bot.cache.InputParamHolder;
import com.openocean.arb.common.constants.YesNoEnum;
import com.openocean.arb.common.util.BigDecimalUtil;
import com.openocean.arb.common.util.CoinUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * arbitrage bot param
 *
 * @author lidong
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyParamDTO {

    // 是否模拟
    private String sampling;
    // 策略触发值
    private BigDecimal triggerValue;
    // 单笔订单金额(报价币)
    private BigDecimal perOrderAmount;
    // dex
    private String address;
    private String privateKey;
    private String dexChainCode;
    private String dexBaseSymbol;
    private String dexQuoteSymbol;
    private BigDecimal dexSlippage;
    // cex
    private String apiKey;
    private String apiSecret;
    private String passPhrase;
    private String exchangeCode;
    private String cexBaseSymbol;
    private String cexQuoteSymbol;
    private String cexMidSymbol;
    private BigDecimal cexFeeRate;
    private Boolean triangular;

    public static StrategyParamDTO create(InputParamHolder holder) {
        return StrategyParamDTO.builder()
                .sampling(StringUtils.defaultString(holder.getSampling(), YesNoEnum.NO.getCode()))
                .triggerValue(BigDecimalUtil.getBigDecimal(holder.getTriggerValue()))
                .perOrderAmount(BigDecimalUtil.getBigDecimal(holder.getPerOrderAmount()))
                .address(holder.getAddress())
                .privateKey(holder.getPrivateKey())
                .dexChainCode(holder.getDexChainCode())
                .dexBaseSymbol(CoinUtil.getUpCoin(holder.getDexPairCode()))
                .dexQuoteSymbol(CoinUtil.getDownCoin(holder.getDexPairCode()))
                .dexSlippage(BigDecimalUtil.getBigDecimal(holder.getDexSlippage()))
                .apiKey(holder.getApiKey())
                .apiSecret(holder.getApiSecret())
                .exchangeCode(holder.getExchangeCode())
                .cexBaseSymbol(CoinUtil.getUpCoin(holder.getCexPairCode()))
                .cexQuoteSymbol(CoinUtil.getDownCoin(holder.getCexPairCode()))
                .cexFeeRate(BigDecimal.valueOf(0.01))
                //.cexMidSymbol()
//                .passPhrase();
                .triangular(false)
                .build();
    }
}
