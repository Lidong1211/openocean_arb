package com.openocean.arb.constants;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Getter
public enum OrderStatusEnum {
    NEW("0", "New"),
    NOT_TRADED("1", "NotFilled"),
    PART_TRADED("2", "PartFilled"),
    CANCELED("3", "Canceled"),
    ALL_TRADED("4", "Filled"),
    FAILED_SEND("5", "Failed to Send"),
    EXPIRED("6", "Expired"); // 订单发送交易所失败

    private static final Map<String, OrderStatusEnum> TYPES;

    static {
        Map<String, OrderStatusEnum> types = new HashMap<>();
        Stream.of(OrderStatusEnum.values()).forEach(type -> types.put(type.code, type));
        TYPES = Collections.unmodifiableMap(types);
    }

    private String code;
    private String message;

    OrderStatusEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static OrderStatusEnum fromCode(String code) {
        return TYPES.getOrDefault(code, null);
    }
}
