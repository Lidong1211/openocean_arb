package com.openocean.arb.constants;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Getter
public enum YesNoEnum {
    /**
     * 否
     */
    NO("0", "No"),
    /**
     * 是
     */
    YES("1", "Yes");

    private static final Map<String, YesNoEnum> TYPES;

    static {
        Map<String, YesNoEnum> types = new HashMap<>();
        Stream.of(YesNoEnum.values()).forEach(type -> types.put(type.code, type));
        TYPES = Collections.unmodifiableMap(types);
    }

    private String code;
    private String message;

    YesNoEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static YesNoEnum fromCode(String code) {
        return TYPES.getOrDefault(code, null);
    }
}
