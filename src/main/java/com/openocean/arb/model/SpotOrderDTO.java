package com.openocean.arb.model;

import com.openocean.arb.model.remote.RemoteOrderQueryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotOrderDTO {
    // 交易日
    private String tradingDay;
    // 本地订单编号
    private String localOrderId;
    // 交易所代码
    private String exchangeCode;
    // 订单编号
    private String orderId;
    // 客户号
    private String clientId;
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
    // 冻结金额
    private BigDecimal frozen;
    // 解冻金额
    private BigDecimal unfrozen;
    // 创建时间
    private Date createTime;
    // 修改时间
    private Date updateTime;

    /**
     * 创建一个实例
     */
    public static SpotOrderDTO create(RemoteOrderQueryResponse result) {
        SpotOrderDTO orderDo = new SpotOrderDTO();
        BeanUtils.copyProperties(result, orderDo);
        return orderDo;
    }
}
