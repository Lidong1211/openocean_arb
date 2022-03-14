package com.openocean.arb.bot.model.remote.openocean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * OpenOceanV2 approve request
 *
 * @author lidong
 **/
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OOV2ApproveReq extends OOV2BaseReq {

    // 平台 openoceanv2
    private String exChange;
    // account
    private String account;
    // inTokenAddress
    private String inTokenAddress;
    // 数量
    private String amount;
    // privateKey
    private String privateKey;
}
