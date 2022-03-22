package com.openocean.arb.constants;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Getter
public enum DirectionEnum {
    BUY("0", "Buy"),
    SELL("1", "Sell");

    private static final Map<String, DirectionEnum> TYPES;

    static {
        Map<String, DirectionEnum> types = new HashMap<>();
        Stream.of(DirectionEnum.values()).forEach(type -> types.put(type.code, type));
        TYPES = Collections.unmodifiableMap(types);
    }

    private String code;
    private String message;

    DirectionEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static DirectionEnum fromCode(String code) {
        return TYPES.getOrDefault(code, null);
    }
}
