package com.openocean.arb.model;

import lombok.Data;

import java.util.List;

/**
 * 订单薄
 *
 * @author lidong
 **/
@Data
public class OrderBookDTO {
    // 买盘
    private List<OrderBookItem> bids;
    // 卖盘
    private List<OrderBookItem> asks;
}
