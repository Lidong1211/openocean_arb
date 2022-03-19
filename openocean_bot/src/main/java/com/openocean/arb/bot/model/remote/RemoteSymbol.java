package com.openocean.arb.bot.model.remote;

import com.openocean.arb.common.constants.CommonConst;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 远程交易对信息
 *
 * @author lidong
 */
@Data
@Builder
public class RemoteSymbol {
    // 交易对 btcusdt
    private String symbol;
    // 价格精度
    private Integer priceScale;
    // 数量精度
    private Integer volumeScale;
    // 最小数量
    private BigDecimal minVolume;

    public static RemoteSymbol create() {
        return RemoteSymbol.builder()
                .priceScale(CommonConst.DEFAULT_SCALE)
                .volumeScale(CommonConst.DEFAULT_SCALE)
                .minVolume(BigDecimal.ONE)
                .build();
    }
}
