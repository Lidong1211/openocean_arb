package com.openocean.arb.common.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 链
 */
public class ChainConst {

    public static Map<String, String> chainTypeMap = new HashMap<>();
    public static Map<String, String> chainIdMap = new HashMap<>();
    public static Map<String, String> networkMap = new HashMap<>();

    static {
        // chainTypeMap
        chainTypeMap.put("bsc", "Binance Smart Chain (BEP20)");
        chainTypeMap.put("eth", "ERC20");
        chainTypeMap.put("ont", "ONT");
        chainTypeMap.put("tron", "TRC20");
        chainTypeMap.put("polygon", "Polygon");
        chainTypeMap.put("fantom", "Fantom");
        // chainIdMap
        chainIdMap.put("eth", "1");
        chainIdMap.put("bsc", "56");
        chainIdMap.put("polygon", "137");
        chainIdMap.put("avax", "43114");
        chainIdMap.put("okex", "66");
        chainIdMap.put("xdai", "100");
        chainIdMap.put("heco", "128");
        chainIdMap.put("fantom", "250");
        chainIdMap.put("avalanche", "43114");
        chainIdMap.put("arbitrum", "42161");
        // network
        networkMap.put("bsc", "BSC");
        networkMap.put("polygon", "MATIC");
        networkMap.put("fantom", "FTM");
    }

}
