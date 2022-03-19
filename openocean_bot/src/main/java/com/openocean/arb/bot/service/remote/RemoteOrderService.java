package com.openocean.arb.bot.service.remote;

import com.openocean.arb.bot.model.remote.*;

import java.util.List;

/**
 * 远程报单服务
 *
 * @author lidong
 */
public interface RemoteOrderService {

    /**
     * 是否可用
     */
    Boolean getEnabled();

    /**
     * 交易所代码
     */
    String getExchangeCode();

    /**
     * 注册API
     */
    void registerApi(RemoteRegisterApiRequest request);

    /**
     * 下单
     */
    RemoteOrderResponse createOrder(RemoteOrderRequest request);

    /**
     * 查询订单
     */
    RemoteOrderQueryResponse getOrder(RemoteOrderQueryRequest request);

    /**
     * 查询资金
     */
    RemoteAssertQueryResponse getAssert(RemoteAssertQueryRequest request);

    /**
     * 取交易对信息
     */
    List<RemoteSymbol> listSymbolInfo();

    /**
     * 取最新价
     */
    List<RemotePrice> listPrice();

    /**
     * 订阅订单薄
     */
    void subscribeOrderBook(String pairCode);

    /**
     * 取订单薄
     */
    RemoteOrderBook getOrderBook(String pairCode);

}
