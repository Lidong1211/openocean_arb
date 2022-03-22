package com.openocean.arb.model.remote;

import com.openocean.arb.model.OrderBookDTO;
import com.openocean.arb.model.OrderBookItem;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单薄
 *
 * @author lidong
 **/
@Data
public class RemoteOrderBook {
    // 买盘
    private List<RemoteOrderBookItem> bids;
    // 卖盘
    private List<RemoteOrderBookItem> asks;

    /**
     * 创建一个OrderBookDTO对象
     */
    public static OrderBookDTO create(RemoteOrderBook orderBook) {
        OrderBookDTO DTO = new OrderBookDTO();
        List<OrderBookItem> bids = orderBook.getBids().parallelStream().map(item -> new OrderBookItem(item.getPrice(), item.getQty())).collect(Collectors.toList());
        List<OrderBookItem> asks = orderBook.getAsks().parallelStream().map(item -> new OrderBookItem(item.getPrice(), item.getQty())).collect(Collectors.toList());
        DTO.setBids(bids);
        DTO.setAsks(asks);
        return DTO;
    }

}
