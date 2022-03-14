package com.openocean.arb.bot.model.remote;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 远程订单查询结果
 *
 * @author lidong
 */
@Data
public class RemoteOrderQueryResponse {
    // 本地订单编号
    private String localOrderId;
    // 交易所代码
    private String exchangeCode;
    // 订单编号
    private String orderId;
    // 币对代码
    private String pairCode;
    // 订单类型
    private String orderType;
    // 买卖方向
    private String direction;
    // 有效期类型
    private String timeCondition;
    // 订单状态
    private String orderStatus;
    // 订单价格
    private BigDecimal orderPrice;
    // 订单数量
    private BigDecimal orderVolume;
    // 成交数量
    private BigDecimal tradeVolume;
    // 成交金额
    private BigDecimal tradeAmount;
    // 手续费
    private BigDecimal fee;
}
