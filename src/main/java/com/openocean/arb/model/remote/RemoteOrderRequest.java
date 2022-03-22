package com.openocean.arb.model.remote;

import com.openocean.arb.model.SpotCreateOrderDTO;
import com.openocean.arb.constants.DirectionEnum;
import com.openocean.arb.constants.OrderTypeEnum;
import com.openocean.arb.constants.TimeConditionEnum;
import com.openocean.arb.util.IdUtil;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 远程报单请求
 *
 * @author lidong
 */
@Data
@Builder
public class RemoteOrderRequest {
    // 交易对：/分割
    private String pairCode;
    // 买卖方向
    private DirectionEnum direction;
    // 报单类型
    private OrderTypeEnum orderType;
    // 有效期类型
    private TimeConditionEnum timeCondition;
    // 价格
    private BigDecimal price;
    // 数量
    private BigDecimal volume;
    // 本地订单编号
    private String localOrderId;

    /**
     * 创建一个实例
     */
    public static RemoteOrderRequest create(SpotCreateOrderDTO DTO) {
        return RemoteOrderRequest.builder()
                .pairCode(DTO.getPairCode())
                .direction(DirectionEnum.fromCode(DTO.getDirection()))
                .orderType(OrderTypeEnum.fromCode(DTO.getOrderType()))
                .timeCondition(TimeConditionEnum.fromCode(DTO.getTimeCondition()))
                .price(DTO.getPrice())
                .volume(DTO.getVolume())
                .localOrderId(IdUtil.genLocalOrderId())
                .build();
    }
}
