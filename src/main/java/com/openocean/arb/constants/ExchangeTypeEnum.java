package com.openocean.arb.constants;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Getter
public enum ExchangeTypeEnum {
    CEX("1", "Cex"),
    DEX("2", "Dex");

    private static final Map<String, ExchangeTypeEnum> TYPES;

    static {
        Map<String, ExchangeTypeEnum> types = new HashMap<>();
        Stream.of(ExchangeTypeEnum.values()).forEach(type -> types.put(type.code, type));
        TYPES = Collections.unmodifiableMap(types);
    }

    private String code;
    private String message;

    ExchangeTypeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ExchangeTypeEnum fromCode(String code) {
        return TYPES.getOrDefault(code, null);
    }
}
