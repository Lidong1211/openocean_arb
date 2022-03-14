package com.openocean.arb.common.constants;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Getter
public enum OrderTypeEnum {
    MARKET("1", "Market"),
    LIMIT("2", "Limit");

    private static final Map<String, OrderTypeEnum> TYPES;

    static {
        Map<String, OrderTypeEnum> types = new HashMap<>();
        Stream.of(OrderTypeEnum.values()).forEach(type -> types.put(type.code, type));
        TYPES = Collections.unmodifiableMap(types);
    }

    private String code;
    private String message;

    OrderTypeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static OrderTypeEnum fromCode(String code) {
        return TYPES.getOrDefault(code, null);
    }
}
