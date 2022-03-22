package com.openocean.arb.model.remote.openocean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenOceanV2 swap response
 *
 * @author lidong
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OOV2SwapResp {
    // 交易Hash
    private String hash;
}
