package com.openocean.arb.bot.model.remote.openocean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * OpenOceanV2 transaction response
 *
 * @author lidong
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OOV2TransactionResp {
    private String hash;
    private String blockNumber;
    private String transactionIndex;
    private String from;
    private String to;
    private String inTokenAddress;
    private String inTokenSymbol;
    private String outTokenAddress;
    private String outTokenSymbol;
    private BigDecimal inAmount;
    private BigDecimal outAmount;
    // 转成usdt的gasfee
    private BigDecimal gasFee;
    private String timestamp;
    private BigDecimal in_amount_value;
    private BigDecimal out_amount_value;
    private BigDecimal in_token_decimals;
    private BigDecimal out_token_decimals;
    private BigDecimal usd_valuation;
    private BigDecimal gasAmount;

}
