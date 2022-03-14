package com.openocean.arb.common.constants;

import lombok.extern.slf4j.Slf4j;

/**
 * 业务码枚举
 *
 * <pre>
 * 响应码新增原则:
 * 1. 在当前响应码不能满足用户提示需求，考虑新增
 * 2. 如果大类能够满足前端的错误处理需求，请勿新增
 * 3. 在不能明确响应的错误分类再考虑新增分类
 * </pre>
 *
 * @author lidong
 */
@Slf4j
public enum BizCodeEnum {

    /**
     * 业务正确响应
     */
    BIZ_SUCCESS(0, "Business submitted successfully"),


    /**
     * 业务相关异常
     */
    BIZ_ERROR(30000, "Business error"),

    /**
     * SAAS异常
     */
    BIZ_ERROR_STRATEGY_IS_RUNNING(31001, "Strategy is running"),
    BIZ_ERROR_EXCHANGE_IS_NOT_SUPPORTED(31002, "CeFi exchange is not supported"),
    BIZ_ERROR_APPROVE_TOKEN_ERROR(31003, "DeFi approve token error"),
    BIZ_ERROR_SYMBOL_NO_MARKET(31004, "CeFi symbol no market data"),
    BIZ_ERROR_TOKEN_NOT_EXISTS(31005, "DeFi token does not exist in the cache"),
    BIZ_ERROR_QUOTE_RESULT_NULL(31006, "DeFi quote result is null"),
    BIZ_ERROR_TX_RESULT_NULL(31007, "DeFi tx result is null"),
    BIZ_ERROR_GET_CEX_RESULT_TIMEOUT(31008, "Get ceFi order result timeout"),

    /**
     * API调用异常
     */
    API_ERROR(40000, "API error"),
    ;


    private Integer code;
    private String message;

    BizCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 错误码
     * @return 错误枚举
     */
    public static BizCodeEnum getEnumByCode(Integer code) {
        BizCodeEnum result = null;
        for (BizCodeEnum bizCodeEnum : BizCodeEnum.values()) {
            if (bizCodeEnum.code.equals(code)) {
                result = bizCodeEnum;
                break;
            }
        }
        return result;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}