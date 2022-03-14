package com.openocean.arb.common.util;

import lombok.experimental.UtilityClass;

/**
 * 币工具类
 **/
@UtilityClass
public class CoinUtil {
    public String getUpCoin(String pairCode) {
        return pairCode == null ? null :
                pairCode.substring(0, pairCode.indexOf("/"));
    }

    public String getDownCoin(String pairCode) {
        return pairCode == null ? null :
                pairCode.substring(pairCode.indexOf("/") + 1);
    }

    public String getPairCode(String baseSymbol, String quoteSymbol) {
        return baseSymbol + "/" + quoteSymbol;
    }

}
