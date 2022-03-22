package com.openocean.arb.model.remote.openocean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * OpenOceanV2 balance request
 *
 * @author lidong
 **/
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OOV2BalanceReq extends OOV2BaseReq {

    private String account;

    private String inTokenAddress;
}
