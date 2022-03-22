package com.openocean.arb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DEX 订单
 *
 * @author lidong
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DexOrderDTO {

    private String txHash;
    private String blockNumber;
    private String txIndex;
    private String address;
    private String sender;
    // 传入token地址
    private String inTokenAddress;
    // 传入token的标识
    private String inTokenSymbol;
    // 传出token的地址
    private String outTokenAddress;
    // 传出token的标识
    private String outTokenSymbol;
    // 传入金额
    private BigDecimal inAmount;
    // 传出金额
    private BigDecimal outAmount;
    // gas费
    private BigDecimal gasFee;

}
