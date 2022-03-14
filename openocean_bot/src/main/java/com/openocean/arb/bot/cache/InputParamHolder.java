package com.openocean.arb.bot.cache;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * holder all inputParams
 *
 * @author lidong
 * @date 2022/3/7
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "input.param")
public class InputParamHolder {
    // 是否模拟
    private String sampling;
    // 策略触发值
    private String triggerValue;
    // 单笔订单金额(报价币)
    private String perOrderAmount;
    // dex
    private String address;
    private String privateKey;
    private String dexChainCode;
    private String dexPairCode;
    private String dexSlippage;
    // cex
    private String apiKey;
    private String apiSecret;
    private String exchangeCode;
    private String cexPairCode;

}
