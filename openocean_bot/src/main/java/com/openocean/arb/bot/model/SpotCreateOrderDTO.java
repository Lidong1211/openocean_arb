package com.openocean.arb.bot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 报单请求
 *
 * @author lidong
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotCreateOrderDTO {
    // 交易对：/分割
    private String pairCode;
    // 买卖方向
    private String direction;
    // 报单类型
    private String orderType;
    // 价格
    private BigDecimal price;
    // 数量；当市价买入时，为交易额
    private BigDecimal volume;
    // 有效期类型
    private String timeCondition;
}
