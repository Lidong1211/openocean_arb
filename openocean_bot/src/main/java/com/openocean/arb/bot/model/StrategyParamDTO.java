package com.openocean.arb.bot.model;

import cn.hutool.core.util.StrUtil;
import com.openocean.arb.bot.client.holder.InputParamHolder;
import com.openocean.arb.common.constants.BizCodeEnum;
import com.openocean.arb.common.constants.YesNoEnum;
import com.openocean.arb.common.exception.BizException;
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

    public static StrategyParamDTO create() {
        // 验证策略信息
        InputParamHolder.StrategyInfo strategyInfo = InputParamHolder.strategyInfo;
        if (strategyInfo == null) {
            throw new BizException(BizCodeEnum.BIZ_ERROR_THERE_IS_NO_EXISTING_STRATEGY);
        }
        StrategyParamDTO param = StrategyParamDTO.builder()
                .sampling(StringUtils.defaultString(strategyInfo.getSampling(), YesNoEnum.NO.getCode()))
                .triggerValue(BigDecimalUtil.getBigDecimal(strategyInfo.getTriggerValue()))
                .perOrderAmount(BigDecimalUtil.getBigDecimal(strategyInfo.getPerOrderAmount()))
                .dexChainCode(strategyInfo.getDexChainCode())
                .dexBaseSymbol(CoinUtil.getUpCoin(strategyInfo.getDexPairCode()))
                .dexQuoteSymbol(CoinUtil.getDownCoin(strategyInfo.getDexPairCode()))
                .dexSlippage(BigDecimalUtil.getBigDecimal(strategyInfo.getDexSlippage()))
                .exchangeCode(strategyInfo.getExchangeCode())
                .cexBaseSymbol(CoinUtil.getUpCoin(strategyInfo.getCexPairCode()))
                .cexQuoteSymbol(CoinUtil.getDownCoin(strategyInfo.getCexPairCode()))
                .cexFeeRate(BigDecimal.valueOf(0.01))
                //.cexMidSymbol()
                //.passPhrase();
                .triangular(false)
                .build();
        if (!StrUtil.equals(strategyInfo.sampling, YesNoEnum.YES.getCode())) {
            // 不是模拟数据则需要验证CeFi链接信息
            String exchangeCode = strategyInfo.getExchangeCode();
            InputParamHolder.CexConnectInfo cexConnectInfo = InputParamHolder.cexConnectMap.get(exchangeCode);
            if (cexConnectInfo == null) {
                throw new BizException(BizCodeEnum.BIZ_ERROR_NOT_FIND_THE_CEX_CONNECT_CONFIG, exchangeCode);
            }
            param.setApiKey(cexConnectInfo.getApiKey());
            param.setApiSecret(cexConnectInfo.getApiSecret());

            // 不是模拟数据则需要验证DeFi链接信息
            String chainCode = strategyInfo.getDexChainCode();
            InputParamHolder.DexConnectInfo dexConnectInfo = InputParamHolder.dexConnectMap.get(chainCode);
            if (dexConnectInfo == null) {
                throw new BizException(BizCodeEnum.BIZ_ERROR_NOT_FIND_THE_DEX_CONNECT_CONFIG, chainCode);
            }
            param.setAddress(dexConnectInfo.getAddress());
            param.setPrivateKey(dexConnectInfo.getPrivateKey());
        }
        return param;
    }
}
