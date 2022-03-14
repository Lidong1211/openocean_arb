package com.openocean.arb.bot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报单响应
 *
 * @author lidong
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotCreateOrderRespDTO {
    // 订单编号
    private String orderId;
    // 本地订单编号
    private String localOrderId;
}
