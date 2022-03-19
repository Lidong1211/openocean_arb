package com.openocean.arb.bot.service.cache;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.openocean.arb.bot.config.remote.openocean.OpenOceanV2Api;
import com.openocean.arb.bot.model.SpotSymbolDTO;
import com.openocean.arb.bot.model.remote.RemotePrice;
import com.openocean.arb.bot.model.remote.RemoteSymbol;
import com.openocean.arb.bot.model.remote.openocean.OOV2BaseResponse;
import com.openocean.arb.bot.model.remote.openocean.OOV2GasPriceResp;
import com.openocean.arb.bot.model.remote.openocean.OOV2TokenReq;
import com.openocean.arb.bot.model.remote.openocean.OOV2TokenResp;
import com.openocean.arb.bot.service.remote.RemoteOrderService;
import com.openocean.arb.common.constants.BizCodeEnum;
import com.openocean.arb.common.constants.ChainConst;
import com.openocean.arb.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 缓存服务
 *
 * @author lidong
 **/
@Slf4j
@Service
public class CacheService {
    @Autowired
    private OpenOceanV2Api openOceanV2Api;
    private Map<String, RemoteOrderService> remoteOrderServices = new HashMap<>();
    @Autowired
    private List<RemoteOrderService> remoteOrderServiceList;

    @PostConstruct
    public void init() {
        if (remoteOrderServiceList != null) {
            remoteOrderServiceList.forEach(service -> remoteOrderServices.put(service.getExchangeCode(), service));
        }
    }

    // 现货交易对缓存 Map的key为交易对，无分隔符，小写，如：btcusdt
    private LoadingCache<String, Map<String, SpotSymbolDTO>> spotSymbolCache =
            Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(60)).build(key -> findAllSpotSymbol(key));
    // 最新价缓存
    private LoadingCache<String, Map<String, BigDecimal>> latestPriceCache =
            Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(1)).build(key -> findAllLatestPrice(key));
    // GasPrice缓存
    private LoadingCache<String, BigDecimal> gasPriceCache =
            Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(1)).build(key -> findOOV2GasPrice(key));
    // Token信息缓存
    private LoadingCache<String, Map<String, OOV2TokenResp>> tokenCache =
            Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(10)).build(key -> findOOV2Token(key));

    /**
     * 取现货交易对
     */
    public SpotSymbolDTO getSpotSymbol(String exchangeCode, String pairCode) {
        SpotSymbolDTO spotSymbol = spotSymbolCache.get(exchangeCode).get(getSymbol(pairCode));
        if (spotSymbol == null) {
            log.error("symbol({}-{}) no symbol info", exchangeCode, pairCode);
            throw new BizException(BizCodeEnum.BIZ_ERROR_SYMBOL_NO_MARKET);
        }
        return spotSymbol;
    }

    /**
     * 刷新所有现货交易对
     */
    private Map<String, SpotSymbolDTO> findAllSpotSymbol(String exchangeCode) {
        RemoteOrderService remoteOrderService = remoteOrderServices.get(exchangeCode);
        List<RemoteSymbol> remoteSymbols = remoteOrderService.listSymbolInfo();
        if (CollectionUtil.isEmpty(remoteSymbols)) {
            return Collections.emptyMap();
        } else {
            return remoteSymbols.parallelStream()
                    .map(s -> SpotSymbolDTO.create(s))
                    .collect(Collectors.toMap(SpotSymbolDTO::getSymbol, Function.identity(), (key1, key2) -> key2));
        }
    }

    /**
     * 获取交易对最新价
     */
    public BigDecimal getLastPrice(String exchangeCode, String pairCode) {
        BigDecimal price = latestPriceCache.get(exchangeCode).get(getSymbol(pairCode));
        if (price == null) {
            log.error("symbol({}-{}) no market info", exchangeCode, pairCode);
            throw new BizException(BizCodeEnum.BIZ_ERROR_SYMBOL_NO_MARKET);
        }
        return price;
    }

    /**
     * 刷新所有交易对最新价
     */
    private Map<String, BigDecimal> findAllLatestPrice(String exchangeCode) {
        RemoteOrderService remoteOrderService = remoteOrderServices.get(exchangeCode);
        List<RemotePrice> remotePrices = remoteOrderService.listPrice();
        if (CollectionUtil.isEmpty(remotePrices)) {
            return Collections.emptyMap();
        } else {
            return remotePrices.parallelStream()
                    .collect(Collectors.toMap(RemotePrice::getSymbol, RemotePrice::getPrice, (key1, key2) -> key2));
        }
    }

    /**
     * 获取GasPrice
     */
    public BigDecimal getGasPrice(String chainCode) {
        return gasPriceCache.get(chainCode);
    }

    /**
     * 刷新链上GasPrice
     */
    private BigDecimal findOOV2GasPrice(String chainCode) {
        String chainId = ChainConst.chainIdMap.get(chainCode);
        while (true) {
            try {
                OOV2BaseResponse<OOV2GasPriceResp> response = openOceanV2Api.getGasPrice(chainId);
                BigDecimal gasPrice = response.getSuccessData().getGasPrice();
                // polygon链默认+5
                if (chainCode.equals("polygon")) {
                    gasPrice = gasPrice.add(BigDecimal.valueOf(5));
                }
                return gasPrice;
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            ThreadUtil.sleep(10 * 1000L);
        }
    }

    /**
     * 获取Token信息
     */
    public OOV2TokenResp getToken(String chainCode, String tokenSymbol) {
        OOV2TokenResp token = tokenCache.get(chainCode).get(tokenSymbol);
        if (token == null) {
            log.error("Token({}-{}) does not exist in the cache", chainCode, tokenSymbol);
            throw new BizException(BizCodeEnum.BIZ_ERROR_TOKEN_NOT_EXISTS);
        }
        return token;
    }

    /**
     * 刷新链上Token信息
     */
    private Map<String, OOV2TokenResp> findOOV2Token(String chainCode) {
        OOV2TokenReq request = OOV2TokenReq.builder().chainId(ChainConst.chainIdMap.get(chainCode)).build();
        OOV2BaseResponse<List<OOV2TokenResp>> response = openOceanV2Api.tokenList(request);
        if (response.isError() || CollectionUtil.isEmpty(response.getData())) {
            return Collections.emptyMap();
        } else {
            return response.getSuccessData().parallelStream()
                    .collect(Collectors.toMap(OOV2TokenResp::getSymbol, Function.identity(), (key1, key2) -> key2));
        }
    }

    private String getSymbol(String pairCode) {
        return pairCode.replaceAll("/", "").toLowerCase();
    }

}
