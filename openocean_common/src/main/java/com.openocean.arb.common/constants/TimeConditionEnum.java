package com.openocean.arb.common.constants;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Getter
public enum TimeConditionEnum {
    GTC("1", "Gtc"),
    IOC("2", "Ioc"),
    FOK("3", "Fok");

    private static final Map<String, TimeConditionEnum> TYPES;

    static {
        Map<String, TimeConditionEnum> types = new HashMap<>();
        Stream.of(TimeConditionEnum.values()).forEach(type -> types.put(type.code, type));
        TYPES = Collections.unmodifiableMap(types);
    }

    private String code;
    private String message;

    TimeConditionEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static TimeConditionEnum fromCode(String code) {
        return TYPES.getOrDefault(code, null);
    }
}
