package com.openocean.arb.bot.model.remote.openocean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * OpenOceanV2 swap request
 *
 * @author lidong
 **/
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OOV2SwapReq extends OOV2BaseReq {

    // 平台 openoceanv2
    private String exChange;
    // 传入token标识
    private String inTokenSymbol;
    // inTokenAddress
    private String inTokenAddress;
    // 传出token标识
    private String outTokenSymbol;
    // outTokenAddress
    private String outTokenAddress;
    // 数量
    private String amount;
    // gasPrice
    private String gasPrice;
    // account
    private String account;
    // privateKey
    private String privateKey;
    // 滑点
    private String slippage;
    // 是否跳过授权
    private String approved;
    // 是否跳过资金验证
    private String withoutCheckBalance;


}
