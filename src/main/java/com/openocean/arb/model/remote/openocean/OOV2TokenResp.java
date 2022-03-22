package com.openocean.arb.model.remote.openocean;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenOceanV2 token response
 *
 * @author lidong
 **/
@Data
@NoArgsConstructor
public class OOV2TokenResp {
    // symbol
    private String symbol;
    // 名称
    private String name;
    // 地址
    private String address;
    // 精度
    private Integer decimals;
}
