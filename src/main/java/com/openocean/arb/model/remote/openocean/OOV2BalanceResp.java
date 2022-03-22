package com.openocean.arb.model.remote.openocean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenOceanV2 balance response
 *
 * @author lidong
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OOV2BalanceResp {
    // 币种
    private String symbol;
    // 余额
    private String balance;

    private String raw;
}
