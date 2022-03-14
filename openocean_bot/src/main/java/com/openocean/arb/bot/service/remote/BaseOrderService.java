package com.openocean.arb.bot.service.remote;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.openocean.arb.bot.model.remote.RemoteOrderBook;
import com.openocean.arb.bot.model.remote.RemoteOrderResponse;
import com.openocean.arb.common.constants.BizCodeEnum;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 订单服务基础类
 *
 * @author lidong
 **/
@Slf4j
public abstract class BaseOrderService {
    protected static final Object KEY = new Object();

    // 订单薄，KEY：交易对（格式与交易所相关）
    protected Cache<String, RemoteOrderBook> orderBooks = Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(5)).build();

    // 订阅订单薄的交易对，KEY：交易对（格式与交易所相关）
    protected Set<String> subscribeSymbols = new HashSet<>();

    /**
     * 开启websocket定时任务
     */
    protected void openWSScheduled() {
        // 每隔1小时重连一次
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> reconnectWebSocket(), 0, 1, TimeUnit.HOURS);
        // 每隔10秒检查行情推送是否正常
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> checkWebSocket(), 10, 10, TimeUnit.SECONDS);
    }

    /**
     * websocket重连
     */
    protected abstract void reconnectWebSocket();

    /**
     * 检查行情推送是否正常
     */
    protected void checkWebSocket() {
        for (String symbol : subscribeSymbols) {
            if (orderBooks.getIfPresent(symbol) == null) {
                reconnectWebSocket();
                return;
            }
        }
    }

    /**
     * 构建下单响应
     */
    protected RemoteOrderResponse buildOrderResponse(String orderId) {
        return RemoteOrderResponse.builder()
                .code(BizCodeEnum.BIZ_SUCCESS.getCode())
                .orderId(orderId)
                .build();
    }

    /**
     * 构建下单响应
     */
    protected RemoteOrderResponse buildOrderResponse(Exception exception) {
        return RemoteOrderResponse.builder()
                .code(BizCodeEnum.API_ERROR.getCode())
                .msg(exception.getMessage())
                .build();
    }

    /**
     * 取小数位数
     */
    protected Integer getScale(String value) {
        BigDecimal bd = new BigDecimal(value);
        if (bd.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        } else if (bd.compareTo(BigDecimal.ONE) < 0) {
            int scale = value.indexOf("1") - value.indexOf(".");
            if (scale < 1) {
                return null;
            } else {
                return scale;
            }
        } else {
            return 0;
        }
    }

}
