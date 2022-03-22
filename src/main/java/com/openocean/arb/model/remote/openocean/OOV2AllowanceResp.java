package com.openocean.arb.model.remote.openocean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

/**
 * OpenOceanV2 allowance response
 *
 * @author lidong
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OOV2AllowanceResp {

    private BigInteger allowance;
}
