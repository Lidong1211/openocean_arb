package com.openocean.arb.bot.model.remote;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 远程资产查询结果
 *
 * @author lidong
 */
@Data
@Builder
public class RemoteAssertQueryResponse {
    // 基础币种资产
    private BigDecimal baseAssert;
    // 基础币种可用
    private BigDecimal baseFree;
    // 基础币种冻结
    private BigDecimal baseLocked;

    // 报价币种资产
    private BigDecimal quoteAssert;
    // 报价币种可用
    private BigDecimal quoteFree;
    // 报价币种冻结
    private BigDecimal quoteLocked;

    public BigDecimal getBaseAssert() {
        return baseAssert != null ? baseAssert : baseFree.add(baseLocked);
    }

    public BigDecimal getQuoteAssert() {
        return quoteAssert != null ? quoteAssert : quoteFree.add(quoteLocked);
    }
}
