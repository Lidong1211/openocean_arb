package com.openocean.arb.service.remote.kucoin;


import cn.hutool.core.collection.CollectionUtil;
import com.kucoin.sdk.KucoinClientBuilder;
import com.kucoin.sdk.KucoinPublicWSClient;
import com.kucoin.sdk.KucoinRestClient;
import com.kucoin.sdk.factory.HttpClientFactory;
import com.kucoin.sdk.model.enums.ApiKeyVersionEnum;
import com.kucoin.sdk.rest.request.OrderCreateApiRequest;
import com.kucoin.sdk.rest.response.*;
import com.kucoin.sdk.websocket.event.Level2Event;
import com.kucoin.sdk.websocket.event.OrderBookEntry;
import com.openocean.arb.client.holder.InputParamHolder;
import com.openocean.arb.constants.*;
import com.openocean.arb.exception.BizException;
import com.openocean.arb.model.remote.*;
import com.openocean.arb.service.remote.BaseOrderService;
import com.openocean.arb.service.remote.RemoteOrderService;
import com.openocean.arb.util.BigDecimalUtil;
import com.openocean.arb.util.CoinUtil;
import com.openocean.arb.util.JacksonUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.kucoin.sdk.constants.APIConstants.API_BASE_URL;

/**
 * Kucoin订单服务
 *
 * @author lidong
 **/
@Slf4j
@Service
public class KucoinOrderService extends BaseOrderService implements RemoteOrderService {
    private KucoinPublicWSClient publicWSClient;
    private KucoinRestClient publicRestClient;
    private KucoinRestClient restClient;
    @Getter
    private String exchangeCode = "kucoin";
    @Getter
    private Boolean enabled = false;

    // @PostConstruct
    public void init() throws IOException {
        InputParamHolder.NetworkProxy proxy = InputParamHolder.networkProxy;
        if (proxy != null) {
            HttpClientFactory.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxy.host, Integer.valueOf(proxy.port))));
        }
        publicWSClient = new KucoinClientBuilder().withBaseUrl(API_BASE_URL).buildPublicWSClient();
        publicRestClient = new KucoinClientBuilder().withBaseUrl(API_BASE_URL).buildRestClient();
    }

    @Override
    public void registerApi(RemoteRegisterApiRequest request) {
        // 注册公共信息restClient
        try {
            init();
        } catch (IOException e) {
            throw new BizException(BizCodeEnum.BIZ_ERROR);
        }
        // 注册现货restClient
        restClient = new KucoinClientBuilder().withBaseUrl(API_BASE_URL)
                .withApiKey(request.getApiKey(), request.getSecret(), request.getPassPhrase())
                .withApiKeyVersion(ApiKeyVersionEnum.V2.getVersion())
                .buildRestClient();
    }

    @Override
    public void ping() {
        try {
            publicRestClient.commonAPI().getServerStatus();
        } catch (IOException e) {
            log.error("kucoin连接异常", e);
            throw new BizException(BizCodeEnum.BIZ_ERROR_EXCHANGE_CONNECT_TIME_OUT);
        }
        openWSScheduled();
    }

    @Override
    public RemoteOrderResponse createOrder(RemoteOrderRequest request) {
        try {
            OrderCreateResponse response = restClient.orderAPI().createOrder(buildOrderCreateRequest(request));
            return buildOrderResponse(response.getOrderId());
        } catch (Exception e) {
            log.error("kucoin下单异常", e);
            return buildOrderResponse(e);
        }
    }

    @Override
    public RemoteOrderQueryResponse getOrder(RemoteOrderQueryRequest request) {
        try {
            OrderResponse response = restClient.orderAPI().getOrder(request.getOrderId());
            return buildOrderQueryResult(response);
        } catch (Exception e) {
            log.error("kucoin取订单({})异常", request.getOrderId(), e);
            return null;
        }
    }

    @Override
    public RemoteAssertQueryResponse getAssert(RemoteAssertQueryRequest request) {
        try {
            String base = CoinUtil.getUpCoin(request.getPairCode());
            String quote = CoinUtil.getDownCoin(request.getPairCode());
            List<AccountBalancesResponse> balances = restClient.accountAPI().listAccounts(null, "trade");
            if (balances != null && CollectionUtil.isNotEmpty(balances)) {
                Map<String, AccountBalancesResponse> balanceMap = balances.stream().collect(Collectors.toMap(
                        AccountBalancesResponse::getCurrency, Function.identity(), (key1, key2) -> key2));
                AccountBalancesResponse baseBalance = balanceMap.get(base);
                AccountBalancesResponse quoteBalance = balanceMap.get(quote);
                return RemoteAssertQueryResponse.builder()
                        .baseAssert(baseBalance.getBalance())
                        .baseFree(baseBalance.getAvailable())
                        .baseLocked(baseBalance.getHolds())
                        .quoteAssert(quoteBalance.getBalance())
                        .quoteFree(quoteBalance.getAvailable())
                        .quoteLocked(quoteBalance.getHolds())
                        .build();
            }
        } catch (Exception e) {
            log.error(String.format("kucoin取%s资金异常", JacksonUtil.toJSONStr(request)), e);
        }
        return null;
    }

    @Override
    public List<RemoteSymbol> listSymbolInfo() {
        try {
            List<SymbolResponse> symbols = publicRestClient.symbolAPI().getSymbols();
            return symbols.stream().map(symbol -> {
                RemoteSymbol remoteSymbol = RemoteSymbol.create();
                remoteSymbol.setSymbol(getSymbolLower(symbol.getSymbol()));
                remoteSymbol.setPriceScale(getScale(symbol.getPriceIncrement().toPlainString()));
                remoteSymbol.setVolumeScale(getScale(symbol.getBaseIncrement().toPlainString()));
                remoteSymbol.setMinVolume(symbol.getBaseMinSize());
                return remoteSymbol;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("kucoin取交易对异常", e);
            return null;
        }
    }

    @Override
    public List<RemotePrice> listPrice() {
        try {
            AllTickersResponse allTickers = publicRestClient.symbolAPI().getAllTickers();
            return allTickers.getTicker().stream().map(ticker -> {
                RemotePrice remotePrice = RemotePrice.create();
                remotePrice.setSymbol(getSymbolLower(ticker.getSymbol()));
                remotePrice.setPrice(ticker.getLast());
                return remotePrice;
            }).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("kucoin取行情快照异常", e);
            return null;
        }
    }

    @Override
    public void subscribeOrderBook(String pairCode) {
        String symbol = getSymbol(pairCode);
        if (subscribeSymbols.contains(symbol)) {
            return;
        }
        subscribeOrderBook(SetUtils.hashSet(symbol));
    }

    @Override
    public RemoteOrderBook getOrderBook(String pairCode) {
        RemoteOrderBook orderBook = orderBooks.getIfPresent(getSymbol(pairCode));
        if (orderBook == null) {
            try {
                OrderBookResponse response = publicRestClient.orderBookAPI().getTop20Level2OrderBook(getSymbol(pairCode));
                orderBook = response == null ? null : createRemoteOrderBook(response.getBids(), response.getAsks());
            } catch (IOException e) {
                log.error("kucoin取({})订单薄异常", pairCode, e);
            }
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
        publicWSClient.onLevel2Data(50, response -> {
            if (response != null) {
                Level2Event level2Event = response.getData();
                if (level2Event != null) {
                    orderBooks.put(getSymbolFromTopic(response.getTopic()),
                            createRemoteOrderBook(level2Event.getBids(), level2Event.getAsks()));
                }
            }
        }, getSubscribeSymbols(symbols));
        subscribeSymbols.addAll(symbols);
    }

    /**
     * 构建下单请求
     */
    private OrderCreateApiRequest buildOrderCreateRequest(RemoteOrderRequest request) {
        return OrderCreateApiRequest.builder()
                .symbol(getSymbol(request.getPairCode()))
                .price(request.getPrice())
                .size(request.getOrderType() == OrderTypeEnum.LIMIT ? request.getVolume() :
                        (request.getDirection() == DirectionEnum.SELL ? request.getVolume() : null))
                .funds(request.getOrderType() == OrderTypeEnum.MARKET && request.getDirection() == DirectionEnum.BUY ? request.getVolume() : null)
                .side(getOrderSide(request.getDirection()))
                .type(getOrderType(request.getOrderType()))
                .timeInForce(getTimeInForce(request.getTimeCondition()))
                .clientOid(request.getLocalOrderId())
                .tradeType("TRADE")
                .build();
    }

    /**
     * 构建订单查询结果
     */
    private RemoteOrderQueryResponse buildOrderQueryResult(OrderResponse order) {
        RemoteOrderQueryResponse result = new RemoteOrderQueryResponse();
        result.setExchangeCode(exchangeCode);
        result.setOrderId(order.getId());
        result.setLocalOrderId(order.getClientOid());
        result.setOrderType(getLocalOrderType(order.getType()));
        result.setDirection(getLocalDirection(order.getSide()));
        result.setOrderStatus(getLocalOrderStatus(order.isActive(), order.isCancelExist()));

        result.setOrderPrice(order.getPrice());
        result.setOrderVolume(order.getSize());
        result.setTradeVolume(order.getDealSize());
        result.setTradeAmount(order.getDealFunds());
        result.setFee(order.getFee());
        return result;
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
        return pairCode.replaceAll("/", "-");
    }

    /**
     * getSymbolLower
     */
    private String getSymbolLower(String symbol) {
        return symbol.replaceAll("-", "").toLowerCase();
    }

    /**
     * 从行情返回数据中取交易对
     *
     * @param topic 如：/spotMarket/level2Depth5:BTC-USDT
     * @return 交易对
     */
    private String getSymbolFromTopic(String topic) {
        return topic.substring(topic.indexOf(":") + 1);
    }

    /**
     * 取订阅的symbols
     */
    private String[] getSubscribeSymbols(Set<String> symbolSet) {
        String[] symbols = new String[symbolSet.size()];
        symbolSet.toArray(symbols);
        return symbols;
    }

    /**
     * 取报单方向
     */
    private String getOrderSide(DirectionEnum direction) {
        switch (direction) {
            case BUY:
                return "buy";
            case SELL:
                return "sell";
            default:
                return null;
        }
    }

    /**
     * 取报单类型
     */
    private String getOrderType(OrderTypeEnum orderType) {
        switch (orderType) {
            case LIMIT:
                return "limit";
            case MARKET:
                return "market";
            default:
                return null;
        }
    }

    /**
     * 取时效策略
     */
    private String getTimeInForce(TimeConditionEnum timeCondition) {
        switch (timeCondition) {
            case GTC:
                return "GTC";
            case IOC:
                return "IOC";
            case FOK:
                return "FOK";
            default:
                return null;
        }
    }

    /**
     * 取本地报单类型
     */
    private String getLocalOrderType(String orderType) {
        if (orderType.equals("limit")) {
            return OrderTypeEnum.LIMIT.getCode();
        } else if (orderType.equals("market")) {
            return OrderTypeEnum.MARKET.getCode();
        } else {
            return null;
        }
    }

    /**
     * 取本地报单方向
     */
    private String getLocalDirection(String direction) {
        if (direction.equals("buy")) {
            return DirectionEnum.BUY.getCode();
        } else if (direction.equals("sell")) {
            return DirectionEnum.SELL.getCode();
        } else {
            return null;
        }
    }

    /**
     * 取本地订单状态
     */
    private String getLocalOrderStatus(boolean isActive, boolean cancelExist) {
        if (cancelExist) {
            return OrderStatusEnum.CANCELED.getCode();
        } else if (isActive) {
            return OrderStatusEnum.NOT_TRADED.getCode();
        } else {
            return OrderStatusEnum.ALL_TRADED.getCode();
        }
    }

    @Override
    protected void reconnectWebSocket() {
        try {
            publicWSClient.reconnect();
            subscribeOrderBook(subscribeSymbols);
            log.info("库币WebSocket重连");
        } catch (Exception e) {
            log.error("库币WebSocket重连异常", e);
        }
    }
}
