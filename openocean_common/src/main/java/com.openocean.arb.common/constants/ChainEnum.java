package com.openocean.arb.common.constants;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Getter
public enum ChainEnum {
    BSC("bsc", "Bsc", "56"),
    ETH("eth", "Eth", "1"),
    ONT("ont", "Ont", ""),
    TRON("tron", "Tron", ""),
    POLYGON("polygon", "Polygon", "137"),
    HECO("heco", "Heco", "128"),
    OKEX("okex", "Okex", "66"),
    AVAX("avax", "Avax", "43114"),
    FANTOM("fantom", "Fantom", "250");

    private static final Map<String, ChainEnum> TYPES;

    static {
        Map<String, ChainEnum> types = new HashMap<>();
        Stream.of(ChainEnum.values()).forEach(type -> types.put(type.code, type));
        TYPES = Collections.unmodifiableMap(types);
    }

    private String code;
    private String message;
    private String id;

    ChainEnum(String code, String message, String id) {
        this.code = code;
        this.message = message;
        this.id = id;
    }

    public static ChainEnum fromCode(String code) {
        return TYPES.getOrDefault(code, null);
    }
}
