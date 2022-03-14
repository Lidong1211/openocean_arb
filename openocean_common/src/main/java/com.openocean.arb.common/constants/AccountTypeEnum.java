package com.openocean.arb.common.constants;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Getter
public enum AccountTypeEnum {
    WALLET("wallet", "Wallet"),
    SPOT("spot", "Spot"),
    PERPETUAL("perpetual", "Perpetual"),
    DELIVERY("delivery", "Delivery");

    private static final Map<String, AccountTypeEnum> TYPES;

    static {
        Map<String, AccountTypeEnum> types = new HashMap<>();
        Stream.of(AccountTypeEnum.values()).forEach(type -> types.put(type.code, type));
        TYPES = Collections.unmodifiableMap(types);
    }

    private String code;
    private String message;

    AccountTypeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static AccountTypeEnum fromCode(String code) {
        return TYPES.getOrDefault(code, null);
    }
}
