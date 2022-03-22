package com.openocean.arb.client.holder;

import cn.hutool.core.map.MapUtil;
import lombok.Data;

import java.util.Map;

/**
 * input params
 *
 * @author lidong
 * @date 2022/3/7
 */
public class InputParamHolder {

    public static StrategyInfo strategyInfo;
    public static NetworkProxy networkProxy;
    public static Map<String, DexConnectInfo> dexConnectMap = MapUtil.newHashMap();
    public static Map<String, CexConnectInfo> cexConnectMap = MapUtil.newHashMap();

    /**
     * 网络代理配置
     */
    @Data
    public static class NetworkProxy {
        public String host;
        public String port;
    }

    /**
     * DeFi 配置信息
     */
    @Data
    public class DexConnectInfo {
        public String chainCode;
        public String address;
        public String privateKey;
    }

    /**
     * CeFi 配置信息
     */
    @Data
    public class CexConnectInfo {
        public String exchangeCode;
        public String apiKey;
        public String apiSecret;
    }

    /**
     * 策略配置信息
     */
    @Data
    public static class StrategyInfo {
        // 是否模拟
        public String sampling;
        // 策略触发值
        public String triggerValue;
        // 单笔订单金额(报价币)
        public String perOrderAmount;
        // dex
        public String dexChainCode;
        public String dexPairCode;
        public String dexSlippage;
        public String dexApiUrl;
        // cex
        public String exchangeCode;
        public String cexPairCode;
    }


}
