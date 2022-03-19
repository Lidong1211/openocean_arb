package com.openocean.arb.bot.service.impl;

import com.openocean.arb.bot.client.holder.InputParamHolder;
import com.openocean.arb.bot.config.remote.openocean.OpenOceanV2Api;
import com.openocean.arb.bot.model.StrategyParamDTO;
import com.openocean.arb.bot.service.StrategyService;
import com.openocean.arb.bot.service.cache.CacheService;
import com.openocean.arb.bot.service.common.StrategyTask;
import com.openocean.arb.bot.service.remote.RemoteOrderService;
import com.openocean.arb.common.constants.BizCodeEnum;
import com.openocean.arb.common.exception.BizException;
import com.openocean.arb.common.util.BotUtil;
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
        StrategyTask task = new StrategyTask(cacheService, openOceanV2Api, remoteOrderServices, executeInterval);
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
