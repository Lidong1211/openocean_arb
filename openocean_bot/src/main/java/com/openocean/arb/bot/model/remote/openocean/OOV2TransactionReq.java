package com.openocean.arb.bot.model.remote.openocean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * OpenOceanV2 transaction request
 *
 * @author lidong
 **/
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OOV2TransactionReq extends OOV2BaseReq {

    private String hash;

    private String exChange;

    private String type;

}
