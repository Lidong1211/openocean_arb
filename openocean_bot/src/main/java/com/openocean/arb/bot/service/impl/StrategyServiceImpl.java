package com.openocean.arb.bot.service.impl;

import com.openocean.arb.bot.cache.CacheService;
import com.openocean.arb.bot.cache.InputParamHolder;
import com.openocean.arb.bot.config.remote.openocean.OpenOceanV2Api;
import com.openocean.arb.bot.model.StrategyParamDTO;
import com.openocean.arb.bot.service.StrategyService;
import com.openocean.arb.bot.service.common.StrategyTask;
import com.openocean.arb.bot.service.remote.RemoteOrderService;
import com.openocean.arb.common.constants.BizCodeEnum;
import com.openocean.arb.common.exception.BizException;
import com.openocean.arb.common.util.BotUtil;
import lombok.extern.slf4j.Slf4j;
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
    @Autowired
    private OpenOceanV2Api openOceanV2Api;
    @Autowired
    private CacheService cacheService;
    @Value("${bot.execute.interval:2}")
    private Long executeInterval;
    @Autowired
    private InputParamHolder inputParamHolder;
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
        StrategyTask task = new StrategyTask(cacheService, openOceanV2Api, remoteOrderServices, executeInterval);
        StrategyParamDTO param = StrategyParamDTO.create(inputParamHolder);
        executor.submit(() -> task.execute(param));
        BotUtil.start();
    }
}
