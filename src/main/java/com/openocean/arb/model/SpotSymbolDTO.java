package com.openocean.arb.model;

import com.openocean.arb.model.remote.RemoteSymbol;
import com.openocean.arb.constants.CommonConst;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;

/**
 * 现货交易对
 *
 * @author lidong
 */
@Data
public class SpotSymbolDTO {
    // 交易对：无分隔符，小写，如：btcusdt
    private String symbol;
    // 价格精度
    private Integer priceScale;
    // 数量精度
    private Integer volumeScale;
    // 最小数量
    private BigDecimal minVolume;

    public static SpotSymbolDTO create(String symbol) {
        SpotSymbolDTO symbolDTO = new SpotSymbolDTO();
        symbolDTO.setSymbol(symbol);
        symbolDTO.setPriceScale(CommonConst.DEFAULT_SCALE);
        symbolDTO.setVolumeScale(CommonConst.DEFAULT_SCALE);
        symbolDTO.setMinVolume(BigDecimal.ONE);
        return symbolDTO;
    }

    public static SpotSymbolDTO create(RemoteSymbol remoteSymbol) {
        SpotSymbolDTO symbolDTO = new SpotSymbolDTO();
        BeanUtils.copyProperties(remoteSymbol, symbolDTO);
        return symbolDTO;
    }

}
