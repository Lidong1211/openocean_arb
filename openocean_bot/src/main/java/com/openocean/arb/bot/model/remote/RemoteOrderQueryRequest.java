package com.openocean.arb.bot.model.remote;

import lombok.Builder;
import lombok.Data;

/**
 * 远程订单查询请求
 *
 * @author lidong
 */
@Data
@Builder
public class RemoteOrderQueryRequest {
    // 客户号
    private String clientId;
    // 交易对：/分割
    private String pairCode;
    // 订单编号
    private String orderId;
    // 本地订单编号
    private String localOrderId;
}
