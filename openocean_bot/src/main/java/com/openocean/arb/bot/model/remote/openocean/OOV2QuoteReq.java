package com.openocean.arb.bot.model.remote.openocean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;


/**
 * OpenOceanV2 quote request
 *
 * @author lidong
 **/
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OOV2QuoteReq extends OOV2BaseReq {

    // 平台 openoceanv2/1inch/paraswap/matcha
    private String exChange;
    // 传入token标识
    private String inTokenSymbol;
    // 传入token地址
    private String inTokenAddress;
    // 传出token标识
    private String outTokenSymbol;
    // 传出token地址
    private String outTokenAddress;
    // 数量
    private String amount;
    // gas费
    private String gasPrice;
    // 滑点
    private String slippage;
}
