package com.openocean.arb.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import com.openocean.arb.client.holder.InputParamHolder;
import com.openocean.arb.config.remote.openocean.OpenOceanV2Api;
import com.openocean.arb.model.StrategyParamDTO;
import com.openocean.arb.model.remote.RemoteRegisterApiRequest;
import com.openocean.arb.service.StrategyService;
import com.openocean.arb.service.cache.CacheService;
import com.openocean.arb.service.common.StrategyTask;
import com.openocean.arb.service.remote.RemoteOrderService;
import com.openocean.arb.constants.BizCodeEnum;
import com.openocean.arb.exception.BizException;
import com.openocean.arb.util.BotUtil;
import com.openocean.arb.util.CoinUtil;
import feign.Feign;
import feign.Logger;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.querymap.BeanQueryMapEncoder;
import feign.slf4j.Slf4jLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 策略服务
 *
 * @author lidong
 **/
@Slf4j
@Service
public class StrategyServiceImpl implements StrategyService {
    private Map<String, RemoteOrderService> remoteOrderServices = new HashMap<>();
    @Autowired
    private List<RemoteOrderService> remoteOrderServiceList;
    private OpenOceanV2Api openOceanV2Api;
    @Autowired
    private CacheService cacheService;
    @Value("${bot.execute.interval:2}")
    private Long executeInterval;
    @Value("${network.remote.openocean.apiV2Url}")
    private String openOceanV2ApiUrl;
    private ExecutorService executor;


    @PostConstruct
    public void init() {
        executor = Executors.newFixedThreadPool(10);
        if (remoteOrderServiceList != null) {
            remoteOrderServiceList.forEach(service -> remoteOrderServices.put(service.getExchangeCode(), service));
        }
    }

    @Override
    public void startStrategy() {
        if (!BotUtil.isStop()) {
            throw new BizException(BizCodeEnum.BIZ_ERROR_STRATEGY_IS_RUNNING);
        }
        StrategyParamDTO param = StrategyParamDTO.create();
        // 配置V2地址
        openOceanV2Api = buildV2Api(InputParamHolder.strategyInfo.getDexApiUrl());
        // 获取CEX远程服务
        RemoteOrderService remoteService = getRemoteService(param);

        StrategyTask task = new StrategyTask(cacheService, openOceanV2Api, remoteService, executeInterval);
        executor.submit(() -> task.execute(param));
        BotUtil.start();
    }

    @Override
    public void stopStrategy() {
        if (BotUtil.isStop()) {
            throw new BizException(BizCodeEnum.BIZ_ERROR_STRATEGY_IS_NOT_RUNNING);
        }
        BotUtil.stop();
    }


    /**
     * 获取 CEX 远程服务
     */
    private RemoteOrderService getRemoteService(StrategyParamDTO strategy) {
        RemoteOrderService remoteOrderService = remoteOrderServices.get(strategy.getExchangeCode());
        if (remoteOrderService == null) {
            log.error("策略没有{}订单服务，策略退出...", strategy.getExchangeCode());
            throw new BizException(BizCodeEnum.BIZ_ERROR_EXCHANGE_IS_NOT_SUPPORTED);
        } else {
            try {
                initRemoteService(strategy, remoteOrderService);
            } catch (Exception e) {
                throw new BizException(BizCodeEnum.BIZ_ERROR_PLEASE_CHECK_YOUR_NETWORK_PROXY_CONFIG);
            }
        }
        return remoteOrderService;
    }

    /**
     * 远程服务初始化
     */
    private void initRemoteService(StrategyParamDTO strategy, RemoteOrderService remoteOrderService) {
        // 注册API KEY
        String apiKey = strategy.getApiKey();
        String apiSecret = strategy.getApiSecret();
        String passPhrase = strategy.getPassPhrase();
        String cexBaseSymbol = strategy.getCexBaseSymbol();
        String cexQuoteSymbol = strategy.getCexQuoteSymbol();
        String cexMidSymbol = strategy.getCexMidSymbol();
        RemoteRegisterApiRequest request = RemoteRegisterApiRequest.builder()
                .apiKey(apiKey)
                .secret(apiSecret)
                .passPhrase(passPhrase)
                .build();
        remoteOrderService.registerApi(request);
        remoteOrderService.ping();
        // 订阅订单薄
        if (strategy.getTriangular()) {
            remoteOrderService.subscribeOrderBook(CoinUtil.getPairCode(cexBaseSymbol, cexMidSymbol));
            remoteOrderService.subscribeOrderBook(CoinUtil.getPairCode(cexQuoteSymbol, cexMidSymbol));
        } else {
            remoteOrderService.subscribeOrderBook(CoinUtil.getPairCode(cexBaseSymbol, cexQuoteSymbol));
        }
        // 等待行情订阅成功
        ThreadUtil.sleep(30 * 1000L);
    }

    /**
     * 自定义v2配置
     */
    private OpenOceanV2Api buildV2Api(String url) {
        url = StringUtils.defaultIfEmpty(url, openOceanV2ApiUrl);
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .client(new OkHttpClient())
                .retryer(new Retryer.Default(100L, 1000L, 1))
                .logger(new Slf4jLogger())
                .logLevel(Logger.Level.FULL)
                .queryMapEncoder(new BeanQueryMapEncoder())
                .target(OpenOceanV2Api.class, url);
    }
}
