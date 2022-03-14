package com.openocean.arb.bot.model.remote.openocean;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenOceanV2 quote response
 *
 * @author lidong
 **/
@Data
public class OOV2QuoteResp {
    // inToken
    private InToken inToken;
    // outToken
    private OutToken outToken;
    // inAmount
    private String inAmount;
    // outAmount
    private String outAmount;
    // transCost
    private String transCost;
    // save
    private String save;
    // exChange
    private String exChange;
    // transUsd
    private String transUsd;

    @Data
    @NoArgsConstructor
    public static class InToken {
        // 币种
        private String symbol;
        // 链id
        private String chainId;
        // 地址
        private String address;
        // inUsd
        private String inUsd;
    }

    @Data
    @NoArgsConstructor
    public static class OutToken {
        // 币种
        private String symbol;
        // 链id
        private String chainId;
        // 地址
        private String address;
        // outUsd
        private String outUsd;
    }

}
