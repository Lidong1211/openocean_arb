package com.openocean.arb.model.remote.openocean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * OpenOceanV2 token request
 *
 * @author lidong
 **/
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OOV2TokenReq extends OOV2BaseReq {

}
