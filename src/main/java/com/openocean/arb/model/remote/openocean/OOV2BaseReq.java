package com.openocean.arb.model.remote.openocean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * OpenOceanV2  request  base
 *
 * @author lidong
 **/
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OOV2BaseReq {

    // 链id
    private String chainId;
}
