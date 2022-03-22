package com.openocean.arb.model.remote.openocean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenOceanV2 transaction receipt response
 *
 * @author lidong
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OOV2TransactionReceiptResp {

    private String status;
}
