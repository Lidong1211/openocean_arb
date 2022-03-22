package com.openocean.arb.model.remote;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 远程价格信息
 *
 * @author lidong
 */
@Data
@Builder
public class RemotePrice {
    // 交易对 btcusdt
    private String symbol;
    // 最新价
    private BigDecimal price;

    public static RemotePrice create() {
        return RemotePrice.builder()
                .price(BigDecimal.ZERO)
                .build();
    }
}
