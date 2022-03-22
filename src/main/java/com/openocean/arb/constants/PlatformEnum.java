package com.openocean.arb.constants;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Getter
public enum PlatformEnum {
    OPEN_OCEAN_V2("openoceanv2", "openoceanv2"),
    ONE_INCH("1inch", "1inch"),
    PARA_SWAP("paraswap", "paraswap"),
    MAT_CHA("matcha", "matcha");


    private static final Map<String, PlatformEnum> TYPES;

    static {
        Map<String, PlatformEnum> types = new HashMap<>();
        Stream.of(PlatformEnum.values()).forEach(type -> types.put(type.code, type));
        TYPES = Collections.unmodifiableMap(types);
    }

    private String code;
    private String message;

    PlatformEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static PlatformEnum fromCode(String code) {
        return TYPES.getOrDefault(code, null);
    }
}
