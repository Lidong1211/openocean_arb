package com.openocean.arb.constants;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Getter
public enum ChainHashStatusEnum {
    NOT_ON_THE_CHAIN("-2", "未上链"),
    PENDING("-1", "等待状态"),
    FAILURE("0", "失败"),
    SUCCESS("1", "成功");

    private static final Map<String, ChainHashStatusEnum> TYPES;

    static {
        Map<String, ChainHashStatusEnum> types = new HashMap<>();
        Stream.of(ChainHashStatusEnum.values()).forEach(type -> types.put(type.code, type));
        TYPES = Collections.unmodifiableMap(types);
    }

    private String code;
    private String message;

    ChainHashStatusEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ChainHashStatusEnum fromCode(String code) {
        return TYPES.getOrDefault(code, null);
    }
}
