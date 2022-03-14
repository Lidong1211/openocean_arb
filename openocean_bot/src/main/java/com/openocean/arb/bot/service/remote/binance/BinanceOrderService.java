package com.openocean.arb.bot.service.remote.binance;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.*;
import com.binance.api.client.domain.account.*;
import com.binance.api.client.domain.account.request.LocalOrderRequest;
import com.binance.api.client.domain.event.DepthStreamsEvent;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.FilterType;
import com.binance.api.client.domain.general.SymbolFilter;
import com.binance.api.client.domain.market.MarketSubscribe;
import com.binance.api.client.domain.market.OrderBookEntry;
import com.binance.api.client.domain.market.TickerPrice;
import com.binance.api.client.exception.BinanceApiException;
import com.binance.api.client.impl.BinanceApiServiceGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openocean.arb.bot.model.remote.*;
import com.openocean.arb.bot.network.OKHttpClientFactory;
import com.openocean.arb.bot.service.remote.BaseOrderService;
import com.openocean.arb.bot.service.remote.RemoteOrderService;
import com.openocean.arb.common.constants.*;
import com.openocean.arb.common.exception.BizException;
import com.openocean.arb.common.util.BigDecimalUtil;
import com.openocean.arb.common.util.CoinUtil;
import com.openocean.arb.common.util.JacksonUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Binance 订单服务
 *
 * @author lidong
 **/
@Slf4j
@Service
public class BinanceOrderService extends BaseOrderService implements RemoteOrderService {
    private BinanceApiWebSocketClient publicWSClient;
    private BinanceApiRestClient publicRestClient;
    private BinanceApiRestClient restClient;
    private WebSocket webSocket;
    @Autowired
    private OKHttpClientFactory okHttpClientFactory;
    @Getter
    private String exchangeCode = "binance";
    // 深度
    private static final String depth = "10";

    @PostConstruct
    public void init() {
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
        BinanceApiServiceGenerator.setOkHttpClient(okHttpClientFactory.getProxyedClient());
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
        publicRestClient = factory.newRestClient();
        publicWSClient = factory.newWebSocketClient();
    }

    @Override
    public void registerApi(RemoteRegisterApiRequest request) {
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(request.getApiKey(), request.getSecret());
        restClient = factory.newRestClient();
        openWSScheduled();
    }

    @Override
    public RemoteOrderResponse createOrder(RemoteOrderRequest request) {
        try {
            NewOrderResponse response = restClient.newOrder(buildNewOrder(request));
            return buildOrderResponse(String.valueOf(response.getOrderId()));
        } catch (BinanceApiException e) {
            log.error("binance下单异常", e);
            return buildOrderResponse(e);
        }

    }

    @Override
    public RemoteOrderQueryResponse getOrder(RemoteOrderQueryRequest request) {
        try {
            Order order = restClient.getOrder(buildLocalOrderRequest(request));
            return buildOrderQueryResult(order);
        } catch (Exception e) {
            log.error("binance取订单({})异常", request.getOrderId(), e);
            return null;
        }
    }

    @Override
    public RemoteAssertQueryResponse getAssert(RemoteAssertQueryRequest request) {
        try {
            String base = CoinUtil.getUpCoin(request.getPairCode());
            String quote = CoinUtil.getDownCoin(request.getPairCode());
            Account account = restClient.getAccount();
            if (account != null && CollectionUtil.isNotEmpty(account.getBalances())) {
                AssetBalance baseBalance = account.getAssetBalance(base);
                AssetBalance quoteBalance = account.getAssetBalance(quote);
                return RemoteAssertQueryResponse.builder()
                        .baseFree(BigDecimalUtil.getBigDecimal(baseBalance.getFree()))
                        .baseLocked(BigDecimalUtil.getBigDecimal(baseBalance.getLocked()))
                        .quoteFree(BigDecimalUtil.getBigDecimal(quoteBalance.getFree()))
                        .quoteLocked(BigDecimalUtil.getBigDecimal(quoteBalance.getLocked()))
                        .build();
            }
        } catch (Exception e) {
            log.error(String.format("binance取%s资金异常", JacksonUtil.toJSONStr(request)), e);
        }
        return null;
    }

    @Override
    public List<RemoteSymbol> listSymbolInfo() {
        try {
            ExchangeInfo exchangeInfo = publicRestClient.getExchangeInfo();
            return exchangeInfo.getSymbols().stream().map(symbol -> {
                Map<FilterType, SymbolFilter> filterMap = symbol.getFilters().stream().collect(Collectors.toMap(
                        SymbolFilter::getFilterType, Function.identity(), (key1, key2) -> key2));
                RemoteSymbol remoteSymbol = RemoteSymbol.create();
                remoteSymbol.setSymbol(getSymbolLower(symbol.getSymbol()));
                // 价格精度
                SymbolFilter priceFilter = filterMap.get(FilterType.PRICE_FILTER);
                if (priceFilter != null) {
                    Integer priceScale = getScale(priceFilter.getMinPrice());
                    if (priceScale != null) {
                        remoteSymbol.setPriceScale(priceScale);
                    }
                }
                // 数量精度
                SymbolFilter amountFilter = filterMap.get(FilterType.LOT_SIZE);
                if (amountFilter != null) {
                    Integer amountScale = getScale(amountFilter.getMinQty());
                    if (amountScale != null) {
                        remoteSymbol.setVolumeScale(amountScale);
                    }
                    remoteSymbol.setMinVolume(BigDecimalUtil.getBigDecimal(amountFilter.getMinQty()));
                }
                return remoteSymbol;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("binance取交易对异常", e);
            return null;
        }
    }

    @Override
    public List<RemotePrice> listPrice() {
        try {
            List<TickerPrice> allPrices = publicRestClient.getAllPrices();
            return allPrices.parallelStream().map(ticker -> {
                RemotePrice remotePrice = RemotePrice.create();
                remotePrice.setSymbol(getSymbolLower(ticker.getSymbol()));
                remotePrice.setPrice(BigDecimalUtil.getBigDecimal(ticker.getPrice()));
                return remotePrice;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("binance取行情快照异常", e);
            return null;
        }

    }

    @Override
    public void subscribeOrderBook(String pairCode) {
        String symbol = getSymbolLower(getSymbol(pairCode));
        if (subscribeSymbols.contains(symbol)) {
            return;
        }
        subscribeOrderBook(SetUtils.hashSet(symbol));
    }

    @Override
    public RemoteOrderBook getOrderBook(String pairCode) {
        String symbol = getSymbolLower(getSymbol(pairCode));
        RemoteOrderBook orderBook = orderBooks.getIfPresent(symbol);
        if (orderBook == null) {
            log.error("binance取({})订单薄异常", pairCode);
            throw new BizException(BizCodeEnum.BIZ_ERROR_SYMBOL_NO_MARKET);
        }
        return orderBook;
    }

    /**
     * 订阅订单薄
     */
    private void subscribeOrderBook(Set<String> symbols) {
        if (CollectionUtil.isEmpty(symbols)) {
            return;
        }
        try {
            MarketSubscribe subscribe = new MarketSubscribe();
            subscribe.setMethod(SubscribeMethod.SUBSCRIBE);
            List<String> params = symbols.stream().map(s -> String.format("%s@depth%s@100ms", s, depth)).collect(Collectors.toList());
            subscribe.setParams(params);
            subscribe.setId(0L);
            ObjectMapper mapper = new ObjectMapper();
            if (webSocket == null) {
                reconnectWebSocket();
            }
            webSocket.send(mapper.writeValueAsString(subscribe));
            // 订阅成功后，添加到该组交易对缓存中
            subscribeSymbols.addAll(symbols);
        } catch (JsonProcessingException e) {
            log.error("订阅币安行情JSON解析异常", e);
        }
    }

    /**
     * 创建RemoteOrderBook
     */
    private RemoteOrderBook createRemoteOrderBook(List<OrderBookEntry> bid, List<OrderBookEntry> ask) {
        RemoteOrderBook DTO = new RemoteOrderBook();
        List<RemoteOrderBookItem> bids = bid.parallelStream()
                .map(entry -> new RemoteOrderBookItem(BigDecimalUtil.getBigDecimal(entry.getPrice()), BigDecimalUtil.getBigDecimal(entry.getQty())))
                .collect(Collectors.toList());
        List<RemoteOrderBookItem> asks = ask.parallelStream()
                .map(entry -> new RemoteOrderBookItem(BigDecimalUtil.getBigDecimal(entry.getPrice()), BigDecimalUtil.getBigDecimal(entry.getQty())))
                .collect(Collectors.toList());
        DTO.setBids(bids);
        DTO.setAsks(asks);
        return DTO;
    }

    /**
     * getSymbol
     */
    private String getSymbol(String pairCode) {
        return pairCode.replaceAll("/", "");
    }

    /**
     * getSymbolLower
     */
    private String getSymbolLower(String symbol) {
        return symbol.toLowerCase();
    }

    /**
     * 从行情返回数据中取交易对
     *
     * @param stream 如：bnbusdt@depth10@100ms
     * @return 交易对
     */
    private String getSymbolFromStream(String stream) {
        return stream.substring(0, stream.indexOf("@"));
    }

    /**
     * 构建下单请求
     */
    private NewOrder buildNewOrder(RemoteOrderRequest request) {
        NewOrder newOrder = new NewOrder(
                getSymbol(request.getPairCode()),
                getOrderSide(request.getDirection()),
                getOrderType(request.getOrderType()),
                getTimeInForce(request.getTimeCondition()),
                request.getVolume().toPlainString(),
                request.getPrice().toPlainString()
        );
        newOrder.newClientOrderId(request.getLocalOrderId())
                .newOrderRespType(NewOrderResponseType.ACK);

        if (request.getOrderType() == OrderTypeEnum.MARKET) {
            newOrder.timeInForce(null);
            newOrder.price(null);
            newOrder.quantity(null);
            if (request.getDirection() == DirectionEnum.BUY) {
                newOrder.quoteOrderQty(request.getVolume().toPlainString());
            } else if (request.getDirection() == DirectionEnum.SELL) {
                newOrder.quantity(request.getVolume().toPlainString());
            }
        }
        return newOrder;
    }

    /**
     * 构建订单查询请求
     */
    private LocalOrderRequest buildLocalOrderRequest(RemoteOrderQueryRequest request) {
        return new LocalOrderRequest(getSymbol(request.getPairCode()))
                .orderId(StrUtil.isBlank(request.getOrderId()) ? null : Long.valueOf(request.getOrderId()))
                .origClientOrderId(StrUtil.isBlank(request.getLocalOrderId()) ? null : request.getLocalOrderId());
    }

    /**
     * 构建订单查询结果
     */
    private RemoteOrderQueryResponse buildOrderQueryResult(Order order) {
        RemoteOrderQueryResponse result = new RemoteOrderQueryResponse();
        result.setExchangeCode(exchangeCode);
        result.setLocalOrderId(order.getClientOrderId());
        result.setOrderId(String.valueOf(order.getOrderId()));
        result.setDirection(getLocalDirection(order.getSide()).getCode());
        result.setOrderType(getLocalOrderType(order.getType()).getCode());
        result.setTimeCondition(getLocalTimeCondition(order.getTimeInForce()).getCode());
        result.setOrderStatus(getLocalOrderStatus(order.getStatus()).getCode());
        result.setOrderPrice(BigDecimalUtil.getBigDecimal(order.getPrice()));
        result.setOrderVolume(BigDecimalUtil.getBigDecimal(order.getOrigQty()));
        result.setTradeVolume(BigDecimalUtil.getBigDecimal(order.getExecutedQty()));
        result.setTradeAmount(BigDecimalUtil.getBigDecimal(order.getCummulativeQuoteQty()));
        return result;
    }

    /**
     * 取报单方向
     */
    private OrderSide getOrderSide(DirectionEnum direction) {
        switch (direction) {
            case BUY:
                return OrderSide.BUY;
            case SELL:
                return OrderSide.SELL;
            default:
                return null;
        }
    }

    /**
     * 取报单类型
     */
    private OrderType getOrderType(OrderTypeEnum orderType) {
        switch (orderType) {
            case MARKET:
                return OrderType.MARKET;
            case LIMIT:
                return OrderType.LIMIT;
            default:
                return null;
        }
    }

    /**
     * 取时效策略
     */
    private TimeInForce getTimeInForce(TimeConditionEnum timeCondition) {
        switch (timeCondition) {
            case GTC:
                return TimeInForce.GTC;
            case IOC:
                return TimeInForce.IOC;
            case FOK:
                return TimeInForce.FOK;
            default:
                return null;
        }
    }

    /**
     * 取本地报单类型
     */
    private OrderTypeEnum getLocalOrderType(OrderType orderType) {
        switch (orderType) {
            case MARKET:
                return OrderTypeEnum.MARKET;
            case LIMIT:
                return OrderTypeEnum.LIMIT;
            default:
                return null;
        }
    }

    /**
     * 取本地报单方向
     */
    private DirectionEnum getLocalDirection(OrderSide orderSide) {
        switch (orderSide) {
            case BUY:
                return DirectionEnum.BUY;
            case SELL:
                return DirectionEnum.SELL;
            default:
                return null;
        }
    }

    private TimeConditionEnum getLocalTimeCondition(TimeInForce timeInForce) {
        switch (timeInForce) {
            case GTC:
                return TimeConditionEnum.GTC;
            case IOC:
                return TimeConditionEnum.IOC;
            case FOK:
                return TimeConditionEnum.FOK;
            default:
                return null;
        }
    }

    /**
     * 取本地订单状态
     */
    private OrderStatusEnum getLocalOrderStatus(OrderStatus orderStatus) {
        switch (orderStatus) {
            case NEW:
                return OrderStatusEnum.NOT_TRADED;
            case PARTIALLY_FILLED:
                return OrderStatusEnum.PART_TRADED;
            case FILLED:
                return OrderStatusEnum.ALL_TRADED;
            default:
                return OrderStatusEnum.CANCELED;
        }
    }

    @Override
    protected void reconnectWebSocket() {
        if (webSocket != null) {
            log.info("行情WebSocket断开重连");
            try {
                webSocket.close(1000, null);
            } catch (Exception e) {
                log.error("行情WebSocket关闭异常", e);
            }
        }
        // 建立链接
        webSocket = publicWSClient.onDepthStreamsEvent(new BinanceApiCallback<DepthStreamsEvent>() {
            @Override
            public void onResponse(DepthStreamsEvent response) {
                if (StrUtil.isNotBlank(response.getStream())) {
                    orderBooks.put(getSymbolFromStream(response.getStream()),
                            createRemoteOrderBook(response.getData().getBids(), response.getData().getAsks()));
                }
            }

            @Override
            public void onFailure(Throwable cause) {
                log.error("处理币安行情推送失败：" + cause.getMessage(), cause);
                reconnectWebSocket();
            }

            @Override
            public void onClosing(int code, String reason) {
                log.error("币安行情推送WebSocket.Closing");
            }

            @Override
            public void onClosed(int code, String reason) {
                log.error("币安行情推送WebSocket.Closed");
            }
        });
        // 发送订阅请求
        subscribeOrderBook(subscribeSymbols);
    }

}
