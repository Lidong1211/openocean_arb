package com.openocean.arb.bot.service.common;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.openocean.arb.bot.config.remote.openocean.OpenOceanV2Api;
import com.openocean.arb.bot.model.*;
import com.openocean.arb.bot.model.remote.*;
import com.openocean.arb.bot.model.remote.openocean.*;
import com.openocean.arb.bot.service.cache.CacheService;
import com.openocean.arb.bot.service.remote.RemoteOrderService;
import com.openocean.arb.common.constants.*;
import com.openocean.arb.common.exception.BizException;
import com.openocean.arb.common.util.BigDecimalUtil;
import com.openocean.arb.common.util.BotUtil;
import com.openocean.arb.common.util.CoinUtil;
import com.openocean.arb.common.util.JacksonUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 策略任务
 *
 * @author lidong
 **/
@Slf4j
public class StrategyTask {
    private RemoteOrderService remoteOrderService;
    private Map<String, RemoteOrderService> remoteOrderServices;
    private CacheService cacheService;
    private OpenOceanV2Api openOceanV2Api;
    private static FundDTO fundCache = null;
    private static Long executeInterval;
    // SWAP交易gas
    private static final BigDecimal CN_GAS_BSC_BNB = new BigDecimal("0.005");
    private static final BigDecimal CN_GAS_POLYGON_MATIC = new BigDecimal("1");


    public StrategyTask(CacheService cacheService, OpenOceanV2Api openOceanV2Api,
                        Map<String, RemoteOrderService> remoteOrderServices,
                        Long executeInterval) {
        this.cacheService = cacheService;
        this.openOceanV2Api = openOceanV2Api;
        this.remoteOrderServices = remoteOrderServices;
        this.executeInterval = executeInterval;
    }

    /**
     * 策略执行
     */
    public void execute(StrategyParamDTO strategy) {
        // 初始化CEX交易所远程服务
        setRemoteOrderService(strategy);

        // 授权DEX币种SWAP交易权限
        approve(strategy);

        while (!BotUtil.isStop()) {
            try {
                // 执行间隔
                ThreadUtil.sleep(executeInterval * 1000);
                StrategyRealDataDTO realDataDTO = StrategyRealDataDTO.create(strategy);

                // DEX->CEX
                dexToCex(strategy, realDataDTO);

                // CEX->DEX
                cexToDex(strategy, realDataDTO);

                // TODO 实时数据写出
                log.info("DEX({})->CEX({}):{}   CEX({})->DEX({}):{}",
                        realDataDTO.getDexBuyPrice(), realDataDTO.getCexSellPrice(), realDataDTO.getDexToCexDiffPct(),
                        realDataDTO.getCexBuyPrice(), realDataDTO.getDexSellPrice(), realDataDTO.getCexToDexDiffPct());
            } catch (Exception e) {
                log.error("arbitrage bot execute error", e);
            }
        }
    }

    /**
     * DEX->CEX
     * 条件：DEX的买价 < CEX的卖价
     * 过程：
     * 1）比较两边的资金量（换算成计价币种比较）
     * 2）如果DEX资金少，则将DEX的计价币换成基础币A，C=MIN(A, CEX的基础币B)，将C在CEX换成计价币（卖出）
     * 比较DEX的买价 与 CEX的卖价，如果符合条件，则进行搬砖
     * 3）如果CEX资金少，则将CEX的基础币换成计价币A（卖出），C=MIN(A，DEX的计价币B)，将C在DEX换成基础币
     * 比较DEX的买价 与 CEX的卖价，如果符合条件，则进行搬砖
     */
    private void dexToCex(StrategyParamDTO strategy, StrategyRealDataDTO realData) {
        // 取资金
        FundDTO fundDTO = getFund(strategy);
        if (fundDTO.getDexQuoteQty().compareTo(BigDecimal.ZERO) <= 0 ||
                fundDTO.getCexBaseQty().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        String cexBaseSymbol = strategy.getCexBaseSymbol();
        BigDecimal cexFeeRate = strategy.getCexFeeRate();
        String dexChainCode = strategy.getDexChainCode();
        String dexBaseSymbol = strategy.getDexBaseSymbol();
        String dexQuoteSymbol = strategy.getDexQuoteSymbol();
        BigDecimal dexSlippage = strategy.getDexSlippage();

        DexQuoteDTO dexQuote;
        CexQuoteDTO cexQuote;
        ExchangeTypeEnum minExchangeType;
        // 将CEX的基础币换算成计价币A
        cexQuote = getCexQuote(DirectionEnum.SELL, fundDTO.getCexBaseQty(), cexFeeRate, strategy);
        // 比较DEX、CEX资金
        if (fundDTO.getDexQuoteQty().compareTo(cexQuote.getAmount()) < 0) {
            // DEX资金少
            minExchangeType = ExchangeTypeEnum.DEX;
            // 将DEX的计价币换成基础币A
            dexQuote = getDexQuote(dexChainCode, dexQuoteSymbol, dexBaseSymbol, fundDTO.getDexQuoteQty(), dexSlippage);
            // C=MIN(A, CEX的基础币B)，将C在CEX卖出
            BigDecimal minBase = BigDecimalUtil.getMin(dexQuote.getOutAmount(), fundDTO.getCexBaseQty());
            cexQuote = getCexQuote(DirectionEnum.SELL, minBase, cexFeeRate, strategy);
        } else {
            // CEX资金少
            minExchangeType = ExchangeTypeEnum.CEX;
            // C=MIN(A，DEX的计价币B)，将C在DEX换成基础币
            dexQuote = getDexQuote(dexChainCode, dexQuoteSymbol, dexBaseSymbol, cexQuote.getAmount(), dexSlippage);
        }

        // DEX的买价、CEX的卖价
        BigDecimal dexBuyPrice = BigDecimalUtil.divide(dexQuote.getInAmount(), dexQuote.getOutAmount());
        BigDecimal cexSellPrice = BigDecimalUtil.divide(cexQuote.getAmount(), cexQuote.getQty());
        log.debug("DEX({}{})买->CEX({}{})卖, {}资金少: dexSlippage={},cexFeeRate={}; " +
                        "DexBuyAmount={},DexBuyQty={},DexBuyPrice={}; CexSellQty={},CexSellAmount={},CexSellPrice={};",
                fundDTO.getDexQuoteQty(), dexQuoteSymbol, fundDTO.getCexBaseQty(), cexBaseSymbol, minExchangeType.getMessage(),
                dexSlippage, cexFeeRate, dexQuote.getInAmount(), dexQuote.getOutAmount(), dexBuyPrice, cexQuote.getQty(),
                cexQuote.getAmount(), cexSellPrice);

        // 触发条件
        BigDecimal profit = checkTrigger(dexBuyPrice, cexSellPrice, strategy.getTriggerValue());
        if (profit != null) {
            log.info("DEX({}{})买->CEX({}{})卖, {}资金少: 触发策略 Profit={},DexBuyPrice={},CexSellPrice={}",
                    fundDTO.getDexQuoteQty(), dexQuoteSymbol, fundDTO.getCexBaseQty(), cexBaseSymbol,
                    minExchangeType.getMessage(), profit, dexBuyPrice, cexSellPrice);
            // 执行策略
            if (strategy.getSampling().equals(YesNoEnum.NO.getCode()) && !BotUtil.isStop()) {
                triggerExecute(strategy, DirectionEnum.BUY, dexQuote.getInAmount(), DirectionEnum.SELL,
                        cexQuote.getLimitPrice(), cexQuote.getQty(), cexQuote.getAmount());
            }
        }

        // 记录实时数据
        realData.setDexBuyPrice(dexBuyPrice);
        realData.setCexSellPrice(cexSellPrice);
        realData.setDexToCexDiff(cexSellPrice.subtract(dexBuyPrice));
    }

    /**
     * CEX->DEX
     * 条件：CEX的买价 < DEX的卖价
     * 过程：
     * 1）比较两边的资金量（换算成计价币种比较）
     * 2）如果DEX资金少，则将DEX的基础币换成计价币A，C=MIN(A, CEX的计价币B), 将C在CEX换成基础币（买入）
     * 比较DEX的卖价 与 CEX的买价，如果符合条件，则进行搬砖
     * 3）如果CEX资金少，则将CEX的计价币换成基础币A（买入），C=MIN(A, DEX的基础币B), 将C在DEX换成计价币
     * 比较DEX的卖价 与 CEX的买价，如果符合条件，则进行搬砖
     */
    private void cexToDex(StrategyParamDTO strategy, StrategyRealDataDTO realData) {
        // 取资金
        FundDTO fundDTO = getFund(strategy);
        if (fundDTO.getDexBaseQty().compareTo(BigDecimal.ZERO) <= 0 ||
                fundDTO.getCexQuoteQty().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        String cexQuoteSymbol = strategy.getCexQuoteSymbol();
        BigDecimal cexFeeRate = strategy.getCexFeeRate();
        String dexChainCode = strategy.getDexChainCode();
        String dexBaseSymbol = strategy.getDexBaseSymbol();
        String dexQuoteSymbol = strategy.getDexQuoteSymbol();
        BigDecimal dexSlippage = strategy.getDexSlippage();

        DexQuoteDTO dexQuote;
        CexQuoteDTO cexQuote;
        ExchangeTypeEnum minExchangeType;
        // 将DEX的基础币换成计价币A
        dexQuote = getDexQuote(dexChainCode, dexBaseSymbol, dexQuoteSymbol, fundDTO.getDexBaseQty(), dexSlippage);
        // 将CEX的计价币换成基础币的总金额
        cexQuote = getCexQuote(DirectionEnum.BUY, fundDTO.getCexQuoteQty(), cexFeeRate, strategy);
        // 比较DEX、CEX的资金
        if (dexQuote.getOutAmount().compareTo(cexQuote.getAmount()) < 0) {
            // DEX资金少
            minExchangeType = ExchangeTypeEnum.DEX;
            // C=MIN(A, CEX的计价币B), 将C在CEX换成基础币（买入）
            BigDecimal minQuote = dexQuote.getOutAmount();
            cexQuote = getCexQuote(DirectionEnum.BUY, minQuote, cexFeeRate, strategy);
        } else {
            // CEX资金少
            minExchangeType = ExchangeTypeEnum.CEX;
            // C=MIN(A, DEX的基础币B), 将C在DEX换成计价币
            BigDecimal minBase = BigDecimalUtil.getMin(cexQuote.getQty(), fundDTO.getDexBaseQty());
            dexQuote = getDexQuote(dexChainCode, dexBaseSymbol, dexQuoteSymbol, minBase, dexSlippage);
        }

        // DEX的卖价、CEX的买价
        BigDecimal dexSellPrice = BigDecimalUtil.divide(dexQuote.getOutAmount(), dexQuote.getInAmount());
        BigDecimal cexBuyPrice = BigDecimalUtil.divide(cexQuote.getAmount(), cexQuote.getQty());
        log.debug("CEX({}{})买->DEX({}{})卖, {}资金少: dexSlippage={},cexFeeRate={}; " +
                        "CexBuyAmount={},CexBuyQty={},CexBuyPrice={}; DexSellQty={},DexSellAmount={},DexSellPrice={};",
                fundDTO.getCexQuoteQty(), cexQuoteSymbol, fundDTO.getDexBaseQty(), dexBaseSymbol, minExchangeType.getMessage(),
                dexSlippage, cexFeeRate, cexQuote.getAmount(), cexQuote.getQty(), cexBuyPrice, dexQuote.getInAmount(),
                dexQuote.getOutAmount(), dexSellPrice);

        // 触发条件
        BigDecimal profit = checkTrigger(cexBuyPrice, dexSellPrice, strategy.getTriggerValue());
        if (profit != null) {
            log.info("CEX({}{})买->DEX({}{})卖, {}资金少: 触发策略 Profit={},CexBuyPrice={},DexSellPrice={}",
                    fundDTO.getCexQuoteQty(), cexQuoteSymbol, fundDTO.getDexBaseQty(), dexBaseSymbol,
                    minExchangeType.getMessage(), profit, cexBuyPrice, dexSellPrice);
            // 执行策略
            if (strategy.getSampling().equals(YesNoEnum.NO.getCode()) && !BotUtil.isStop()) {
                triggerExecute(strategy, DirectionEnum.SELL, dexQuote.getInAmount(),
                        DirectionEnum.BUY, cexQuote.getLimitPrice(), cexQuote.getQty(), cexQuote.getAmount());
            }
        }

        // 记录实时数据
        realData.setCexBuyPrice(cexBuyPrice);
        realData.setDexSellPrice(dexSellPrice);
        realData.setCexToDexDiff(dexSellPrice.subtract(cexBuyPrice));
    }

    /**
     * 设置 CEX 远程服务
     */
    private void setRemoteOrderService(StrategyParamDTO strategy) {
        remoteOrderService = remoteOrderServices.get(strategy.getExchangeCode());
        if (remoteOrderService == null) {
            log.error("策略没有{}订单服务，策略退出...", strategy.getExchangeCode());
            throw new BizException(BizCodeEnum.BIZ_ERROR_EXCHANGE_IS_NOT_SUPPORTED);
        } else {
            initRemoteService(strategy);
        }
    }

    /**
     * 授权DEX币种SWAP交易权限
     */
    private void approve(StrategyParamDTO param) {
        // 模拟数据不需要授权
        if (param.getSampling().equals(YesNoEnum.YES.getCode())) {
            return;
        }
        String address = param.getAddress();
        String privateKey = param.getPrivateKey();
        String dexChainCode = param.getDexChainCode();
        String dexBaseSymbol = param.getDexBaseSymbol();
        String dexQuoteSymbol = param.getDexQuoteSymbol();
        List<String> symbols = CollectionUtil.newArrayList(dexBaseSymbol, dexQuoteSymbol);

        OOV2ApproveReq request = OOV2ApproveReq.builder()
                .exChange(PlatformEnum.OPEN_OCEAN_V2.getCode())
                .chainId(ChainConst.chainIdMap.get(dexChainCode))
                .privateKey(privateKey)
                .account(address)
                .amount("1")
                .build();
        for (String symbol : symbols) {
            OOV2TokenResp token = cacheService.getToken(dexChainCode, symbol);
            request.setInTokenAddress(token.getAddress());
            int count = 0;
            Boolean isApproved = false;
            while (count < 3 && !isApproved) {
                try {
                    isApproved = isApproved(request);
                    // 未做授权
                    if (!isApproved) {
                        OOV2BaseResponse response = openOceanV2Api.approve(request);
                        response.getSuccessData();
                        // 查询是否授权成功，5分钟时间
                        long startTime = System.currentTimeMillis();
                        while (System.currentTimeMillis() - startTime < 5 * 60 * 1000L && !isApproved) {
                            isApproved = isApproved(request);
                            ThreadUtil.sleep(30 * 1000L);
                        }
                        if (!isApproved) {
                            log.info("arbitrage bot approve token({}) timeout", symbol);
                        }
                    }
                } catch (Exception e) {
                    log.error("arbitrage bot approve token({}) error", symbol, e.getMessage());
                    ThreadUtil.sleep(60 * 1000L);
                }
                count++;
            }
            if (!isApproved) {
                throw new BizException(BizCodeEnum.BIZ_ERROR_APPROVE_TOKEN_ERROR);
            }
        }
    }

    /**
     * 是否已授权
     */
    private boolean isApproved(OOV2ApproveReq request) {
        try {
            // 判断是否已做授权
            OOV2BaseResponse<OOV2AllowanceResp> allowanceResponse = openOceanV2Api.allowance(request);
            BigInteger allowance = allowanceResponse.getSuccessData().getAllowance();
            return allowance.compareTo(BigInteger.ZERO) > 0;
        } catch (Exception e) {
            log.error("openocean allowance error", e.getMessage());
        }
        return false;
    }

    /**
     * 取资金
     */
    private FundDTO getFund(StrategyParamDTO strategy) {
        if (fundCache != null) {
            return fundCache;
        }
        // CEX Params
        String exchangeCode = strategy.getExchangeCode();
        String cexBaseSymbol = strategy.getCexBaseSymbol();
        String cexQuoteSymbol = strategy.getCexQuoteSymbol();
        // DEX Params
        String dexBaseSymbol = strategy.getDexBaseSymbol();
        String dexQuoteSymbol = strategy.getDexQuoteSymbol();
        BigDecimal dexSlippage = strategy.getDexSlippage();
        String dexChainCode = strategy.getDexChainCode();
        String dexAddress = strategy.getAddress();

        FundDTO fundDTO = FundDTO.createEmpty();
        fundDTO.setDexAddress(strategy.getAddress());
        fundDTO.setPrivateKey(strategy.getPrivateKey());
        // 每单金额限制
        BigDecimal quoteQtyLimit = strategy.getPerOrderAmount();
        BigDecimal baseQtyLimit = getDexQuote(dexChainCode, dexQuoteSymbol, dexBaseSymbol, quoteQtyLimit, dexSlippage).getOutAmount();

        if (strategy.getSampling().equals(YesNoEnum.YES.getCode())) {
            // 模拟数据
            fundDTO.setCexBaseQty(baseQtyLimit);
            fundDTO.setCexQuoteQty(quoteQtyLimit);
            fundDTO.setDexBaseQty(baseQtyLimit);
            fundDTO.setDexQuoteQty(quoteQtyLimit);
        } else {
            // CEX Fund
            RemoteAssertQueryRequest assertRequest = RemoteAssertQueryRequest.builder()
                    .pairCode(CoinUtil.getPairCode(cexBaseSymbol, cexQuoteSymbol))
                    .build();
            RemoteAssertQueryResponse assertResp = remoteOrderService.getAssert(assertRequest);
            log.info("获取CeFi({})账户余额: {} {}/{} {}", exchangeCode,
                    assertResp.getBaseFree(), cexBaseSymbol, assertResp.getQuoteFree(), cexQuoteSymbol);
            if (assertResp != null) {
                // CEX限制下单金额, 如果小于每单金额，则设置为0
                fundDTO.setCexBaseQty(getOrderAvailQty(assertResp.getBaseFree(), baseQtyLimit));
                fundDTO.setCexQuoteQty(getOrderAvailQty(assertResp.getQuoteFree(), quoteQtyLimit));
            }

            // DEX Fund
            List<String> symbols = Arrays.asList(dexBaseSymbol, dexQuoteSymbol);
            Map<String, BigDecimal> dexFundMap = getDexFund(dexAddress, dexChainCode, symbols);
            BigDecimal dexBaseFree = remainGasFee(dexChainCode, dexBaseSymbol, dexFundMap.get(dexBaseSymbol));
            BigDecimal dexQuoteFree = remainGasFee(dexChainCode, dexQuoteSymbol, dexFundMap.get(dexQuoteSymbol));
            log.info("获取DeFi({})账户余额: {} {}/{} {}", dexChainCode,
                    dexBaseFree, dexBaseSymbol, dexQuoteFree, dexQuoteSymbol);
            // DEX限制下单金额,
            fundDTO.setDexBaseQty(getOrderAvailQty(dexBaseFree, baseQtyLimit));
            fundDTO.setDexQuoteQty(getOrderAvailQty(dexQuoteFree, quoteQtyLimit));
        }
        fundCache = fundDTO;
        return fundCache;
    }

    /**
     * 获取币种本单可用金额
     * 如果账户余额 < 单个订单金额，则设置为 0
     */
    private BigDecimal getOrderAvailQty(BigDecimal free, BigDecimal limit) {
        return free.compareTo(limit) < 0 ? BigDecimal.ZERO : limit;
    }

    /**
     * 获取DEX账户余额
     */
    private Map<String, BigDecimal> getDexFund(String dexAddress, String dexChainCode, List<String> symbols) {
        List<String> tokenAddressList = CollectionUtil.newArrayList();
        Map<String, Integer> tokenDecimalsMap = CollectionUtil.newHashMap();
        for (String symbol : symbols) {
            OOV2TokenResp token = cacheService.getToken(dexChainCode, symbol);
            tokenAddressList.add(token.getAddress());
            tokenDecimalsMap.put(token.getSymbol(), token.getDecimals());
        }
        OOV2BalanceReq request = OOV2BalanceReq.builder()
                .inTokenAddress(StrUtil.join(CommonConst.COMMA_STR, tokenAddressList))
                .chainId(ChainConst.chainIdMap.get(dexChainCode))
                .account(dexAddress)
                .build();
        OOV2BaseResponse<List<OOV2BalanceResp>> balanceResp = openOceanV2Api.getBalance(request);
        List<OOV2BalanceResp> balanceResults = balanceResp.getSuccessData();
        return balanceResults.parallelStream().collect(Collectors.toMap(OOV2BalanceResp::getSymbol,
                s -> BigDecimalUtil.getManualAmount(s.getRaw(), tokenDecimalsMap.get(s.getSymbol()))));
    }

    /**
     * 取订单薄
     */
    private OrderBookDTO getOrderBook(String baseSymbol, String quoteSymbol) {
        RemoteOrderBook remoteOrderBook = remoteOrderService.getOrderBook(CoinUtil.getPairCode(baseSymbol, quoteSymbol));
        if (remoteOrderBook == null) {
            throw new BizException(BizCodeEnum.BIZ_ERROR_SYMBOL_NO_MARKET);
        }
        return RemoteOrderBook.create(remoteOrderBook);
    }

    /**
     * 取CEX报价
     */
    private CexQuoteDTO getCexQuote(DirectionEnum direction, BigDecimal amount, BigDecimal feeRate, StrategyParamDTO strategy) {
        if (strategy.getTriangular()) {
            // 三角套利
            OrderBookDTO baseOrderBook = getOrderBook(strategy.getCexBaseSymbol(), strategy.getCexMidSymbol());
            OrderBookDTO quoteOrderBook = getOrderBook(strategy.getCexQuoteSymbol(), strategy.getCexMidSymbol());
            if (direction == DirectionEnum.BUY) {
                // 买：先卖报价币，再买基础币
                CexQuoteDTO quoteQuote = getCexQuote(quoteOrderBook, DirectionEnum.SELL, amount, feeRate);
                CexQuoteDTO baseQuote = getCexQuote(baseOrderBook, DirectionEnum.BUY, quoteQuote.getAmount(), feeRate);
                return new CexQuoteDTO(baseQuote.getQty(), quoteQuote.getQty(), null);
            } else {
                // 卖：先卖基础币，再买报价币
                CexQuoteDTO baseQuote = getCexQuote(baseOrderBook, DirectionEnum.SELL, amount, feeRate);
                CexQuoteDTO quoteQuote = getCexQuote(quoteOrderBook, DirectionEnum.BUY, baseQuote.getAmount(), feeRate);
                return new CexQuoteDTO(baseQuote.getQty(), quoteQuote.getQty(), null);
            }
        } else {
            OrderBookDTO orderBook = getOrderBook(strategy.getCexBaseSymbol(), strategy.getCexQuoteSymbol());
            return getCexQuote(orderBook, direction, amount, feeRate);
        }
    }

    /**
     * 取CEX报价（通过订单薄计算）
     */
    private CexQuoteDTO getCexQuote(OrderBookDTO orderBook, DirectionEnum direction, BigDecimal amount, BigDecimal feeRate) {
        // 买
        if (direction == DirectionEnum.BUY) {
            // 取卖盘
            List<OrderBookItem> asks = orderBook.getAsks();
            BigDecimal totalAmt = BigDecimal.ZERO;
            BigDecimal totalQty = BigDecimal.ZERO;
            for (OrderBookItem item : asks) {
                BigDecimal amt = item.getPrice().multiply(item.getQty());
                if (totalAmt.add(amt).compareTo(amount) >= 0) {
                    BigDecimal left = amount.subtract(totalAmt);
                    BigDecimal qty = left.divide(item.getPrice(), 8, BigDecimal.ROUND_DOWN);
                    return new CexQuoteDTO(getTradeAmt(totalQty.add(qty), feeRate), amount, item.getPrice());
                } else {
                    totalAmt = totalAmt.add(amt);
                    totalQty = totalQty.add(item.getQty());
                }
            }
            // 盘口深度不够，则返回盘口累计数量、金额
            return new CexQuoteDTO(getTradeAmt(totalQty, feeRate), totalAmt, getLimitPrice(asks));
        }

        // 卖
        if (direction == DirectionEnum.SELL) {
            // 取买盘
            List<OrderBookItem> bids = orderBook.getBids();
            BigDecimal totalQty = BigDecimal.ZERO;
            BigDecimal totalAmt = BigDecimal.ZERO;
            for (OrderBookItem item : bids) {
                if (totalQty.add(item.getQty()).compareTo(amount) >= 0) {
                    BigDecimal left = amount.subtract(totalQty);
                    BigDecimal amt = totalAmt.add(item.getPrice().multiply(left));
                    return new CexQuoteDTO(amount, getTradeAmt(amt, feeRate), item.getPrice());
                } else {
                    totalQty = totalQty.add(item.getQty());
                    totalAmt = totalAmt.add(item.getPrice().multiply(item.getQty()));
                }
            }
            // 盘口深度不够，则返回盘口累计数量、金额
            return new CexQuoteDTO(totalQty, getTradeAmt(totalAmt, feeRate), getLimitPrice(bids));
        }
        throw new BizException(BizCodeEnum.BIZ_ERROR);
    }

    /**
     * 取DEX报价
     */
    public DexQuoteDTO getDexQuote(String chainCode, String inSymbol, String outSymbol, BigDecimal amount, BigDecimal slippage) {
        OOV2TokenResp inToken = cacheService.getToken(chainCode, inSymbol);
        OOV2TokenResp outToken = cacheService.getToken(chainCode, outSymbol);
        BigDecimal gasPrice = cacheService.getGasPrice(chainCode);

        OOV2QuoteReq request = OOV2QuoteReq.builder()
                .inTokenSymbol(inToken.getSymbol())
                .inTokenAddress(inToken.getAddress())
                .outTokenSymbol(outToken.getSymbol())
                .outTokenAddress(outToken.getAddress())
                .chainId(ChainConst.chainIdMap.get(chainCode))
                .exChange(PlatformEnum.OPEN_OCEAN_V2.getCode())
                .amount(amount.toPlainString())
                .gasPrice(gasPrice.toPlainString())
                .slippage(getDexSlippage(slippage))
                .build();
        OOV2BaseResponse<OOV2QuoteResp> response = openOceanV2Api.getQuote(request);
        OOV2QuoteResp quoteResult = response.getSuccessData();
        if (quoteResult != null) {
            BigDecimal outAmount = getTradeAmt(BigDecimalUtil.getBigDecimal(quoteResult.getOutAmount()), slippage);
            return new DexQuoteDTO(amount, outAmount);
        } else {
            throw new BizException(BizCodeEnum.BIZ_ERROR_QUOTE_RESULT_NULL);
        }
    }

    /**
     * 触发策略执行
     */
    private void triggerExecute(StrategyParamDTO strategy, DirectionEnum dexDirection, BigDecimal dexAmount, DirectionEnum cexDirection,
                                BigDecimal cexLimitPrice, BigDecimal cexVolume, BigDecimal cexAmount) {
        fundCache = null;
        String exchangeCode = strategy.getExchangeCode();
        String cexBaseSymbol = strategy.getCexBaseSymbol();
        String cexQuoteSymbol = strategy.getCexQuoteSymbol();
        String cexPairCode = CoinUtil.getPairCode(cexBaseSymbol, cexQuoteSymbol);
        String dexBaseSymbol = strategy.getDexBaseSymbol();
        String dexQuoteSymbol = strategy.getDexQuoteSymbol();
        String dexChainCode = strategy.getDexChainCode();
        BigDecimal dexSlippage = strategy.getDexSlippage();
        String address = strategy.getAddress();
        String privateKey = strategy.getPrivateKey();
        // 验证CEX报单金额
        if (!checkOrder(exchangeCode, cexPairCode, cexVolume)) {
            return;
        }
        BotUtil.triggerAdd();

        /**
         * 发送DEX Swap指令
         */
        String inSymbol = dexDirection == DirectionEnum.SELL ? dexBaseSymbol : dexQuoteSymbol;
        String outSymbol = dexDirection == DirectionEnum.SELL ? dexQuoteSymbol : dexBaseSymbol;
        String txHash = sendDexSwap(dexChainCode, inSymbol, outSymbol, dexAmount, dexSlippage, privateKey, address, 1);
        if (txHash == null) {
            log.error("执行Swap异常: {}-{}-{}-{}-{}", dexChainCode, inSymbol, outSymbol, dexAmount);
            return;
        }

        /**
         * 发送CEX报单
         */
        SpotOrderDTO cexOrder = cexCreateOrder(strategy, cexDirection, cexLimitPrice, cexVolume, cexAmount, false);
        if (cexOrder == null) {
            return;
        }

        /**
         * 查询DEX SWAP交易状态
         */
        DexOrderDTO dexOrder = null;
        ChainHashStatusEnum dexSwapStatus = dexTxIsSuccess(dexChainCode, txHash);
        if (dexSwapStatus == ChainHashStatusEnum.SUCCESS) {
            log.info("DEX交易已成功 {}-{}", dexChainCode, txHash);
            dexOrder = dexCreateOrder(dexChainCode, txHash);
        }

        /**
         * 策略执行结果
         */
        StrategyResultDTO resultDTO = new StrategyResultDTO();
        BeanUtil.copyProperties(strategy, resultDTO);
        resultDTO.setCexDirection(cexDirection.getCode());
        resultDTO.setDexDirection(dexDirection.getCode());
        resultDTO.setGasFee(dexOrder.getGasFee());
        resultDTO.setDexAmount(dexDirection == DirectionEnum.BUY ? dexOrder.getInAmount() : dexOrder.getOutAmount());
        resultDTO.setDexQty(dexDirection == DirectionEnum.BUY ? dexOrder.getOutAmount() : dexOrder.getInAmount());
        resultDTO.setDexPrice(BigDecimalUtil.divide(resultDTO.getDexAmount(), resultDTO.getDexQty()));
        resultDTO.setCexQty(cexDirection == DirectionEnum.BUY ? cexOrder.getTradeVolume().subtract(cexOrder.getFee()) : cexOrder.getTradeVolume());
        resultDTO.setCexAmount(cexDirection == DirectionEnum.BUY ? cexOrder.getTradeAmount() : cexOrder.getTradeAmount().subtract(cexOrder.getFee()));
        resultDTO.setCexPrice(BigDecimalUtil.divide(resultDTO.getCexAmount(), resultDTO.getCexQty()));

        /**
         * 计算盈利
         * 基础币种变化：买数量 - 卖数量
         * 报价币种变化：卖金额 - 买金额
         */
        BigDecimal baseChange = resultDTO.getDexQty().subtract(resultDTO.getCexQty());
        BigDecimal quoteChange = resultDTO.getCexAmount().subtract(resultDTO.getDexAmount());
        // DEX（买）->CEX（卖） /  CEX（买）->DEX（卖）
        Boolean isDexToCex = dexDirection == DirectionEnum.BUY && cexDirection == DirectionEnum.SELL;
        resultDTO.setBaseProfit(isDexToCex ? baseChange : baseChange.negate());
        resultDTO.setQuoteProfit(isDexToCex ? quoteChange : quoteChange.negate());
        BotUtil.triggerClear();

        log.info("策略执行结果：{},BuyAmount={},BuyQty={},SellAmount={},SellQty={}; BaseChange={}{},QuoteChange={}{}",
                isDexToCex ? "DEX->CEX" : "CEX->DEX",
                isDexToCex ? resultDTO.getDexAmount() : resultDTO.getCexAmount(),
                isDexToCex ? resultDTO.getDexQty() : resultDTO.getCexQty(),
                isDexToCex ? resultDTO.getCexAmount() : resultDTO.getDexAmount(),
                isDexToCex ? resultDTO.getCexQty() : resultDTO.getDexQty(),
                resultDTO.getBaseProfit().toPlainString(), cexBaseSymbol, resultDTO.getQuoteProfit().toPlainString(), cexQuoteSymbol);

    }

    /**
     * 组装 DEX订单
     */
    private DexOrderDTO dexCreateOrder(String dexChainCode, String txHash) {
        // 从链上获取交易信息
        OOV2TransactionResp result = null;
        OOV2BaseResponse<OOV2TransactionResp> response = null;
        while (Objects.isNull(response) || response.isError()) {
            try {
                OOV2TransactionReq request = OOV2TransactionReq.builder()
                        .chainId(ChainConst.chainIdMap.get(dexChainCode))
                        .exChange(PlatformEnum.OPEN_OCEAN_V2.getCode())
                        .type("swap").hash(txHash)
                        .build();
                response = openOceanV2Api.getTransaction(request);
                result = response.getSuccessData();
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("从链({})上获取({})的交易信息 {}", dexChainCode, txHash, JacksonUtil.toJSONStr(response));
        }

        //创建dex订单
        return DexOrderDTO.builder()
                .inTokenSymbol(result.getInTokenSymbol())
                .outTokenSymbol(result.getOutTokenSymbol())
                .inAmount(result.getIn_amount_value())
                .outAmount(result.getOut_amount_value())
                .blockNumber(result.getBlockNumber())
                .txIndex(result.getTransactionIndex())
                .inTokenAddress(result.getFrom())
                .outTokenAddress(result.getTo())
                .address(result.getFrom())
                .sender(result.getFrom())
                .gasFee(result.getGasFee())
                .txHash(txHash)
                .build();
    }

    /**
     * 创建CEX订单
     */
    private SpotOrderDTO cexCreateOrder(StrategyParamDTO strategy, DirectionEnum direction, BigDecimal price,
                                        BigDecimal volume, BigDecimal amount, boolean isReverse) {
        String exchangeCode = strategy.getExchangeCode();
        String cexBaseSymbol = strategy.getCexBaseSymbol();
        String cexQuoteSymbol = strategy.getCexQuoteSymbol();
        String cexMidSymbol = strategy.getCexMidSymbol();
        Boolean triangular = strategy.getTriangular();
        BigDecimal cexFeeRate = strategy.getCexFeeRate();
        SpotOrderDTO resultOrder = null;
        if (triangular) {
            // 三角套利，CEX发市价单
            OrderTypeEnum orderType = OrderTypeEnum.MARKET;
            SpotOrderDTO quoteOrder = null;
            SpotOrderDTO baseOrder = null;
            BigDecimal secondTradeAmount = null; // 第二次交易金额
            String quotePairCode = CoinUtil.getPairCode(cexQuoteSymbol, cexMidSymbol);
            String basePairCode = CoinUtil.getPairCode(cexBaseSymbol, cexMidSymbol);
            if (direction == DirectionEnum.BUY) {
                // 买：先卖报价币，再买基础币
                BigDecimal quotePrice = cacheService.getLastPrice(exchangeCode, quotePairCode);
                quoteOrder = cexCreateOrder(exchangeCode, quotePairCode, DirectionEnum.SELL, orderType, null, amount, amount.multiply(quotePrice), isReverse);
                secondTradeAmount = getTradeAmt(quoteOrder.getTradeAmount(), cexFeeRate);
                if (quoteOrder != null) {
                    baseOrder = cexCreateOrder(exchangeCode, basePairCode, DirectionEnum.BUY, orderType, null, null, secondTradeAmount, isReverse);
                }
            } else {
                // 卖：先卖基础币，再买报价币
                BigDecimal basePrice = cacheService.getLastPrice(exchangeCode, basePairCode);
                baseOrder = cexCreateOrder(exchangeCode, basePairCode, DirectionEnum.SELL, orderType, null, volume, volume.multiply(basePrice), isReverse);
                secondTradeAmount = getTradeAmt(baseOrder.getTradeAmount(), cexFeeRate);
                if (baseOrder != null) {
                    quoteOrder = cexCreateOrder(exchangeCode, quotePairCode, DirectionEnum.BUY, orderType, null, null, secondTradeAmount, isReverse);
                }
            }
            if (quoteOrder != null && baseOrder != null) {
                resultOrder = new SpotOrderDTO();
                resultOrder.setTradeVolume(baseOrder.getTradeVolume());
                resultOrder.setTradeAmount(quoteOrder.getTradeVolume());
            }
        } else {
            // OrderTypeEnum orderType = isReverse ? OrderTypeEnum.MARKET : OrderTypeEnum.LIMIT;
            OrderTypeEnum orderType = OrderTypeEnum.MARKET;
            String pairCode = CoinUtil.getPairCode(cexBaseSymbol, cexQuoteSymbol);
            resultOrder = cexCreateOrder(exchangeCode, pairCode, direction, orderType, price, volume, amount, isReverse);
        }
        if (resultOrder != null) {
            // 由于各个交易所收取手续费的方式不同，所以手续费统一处理
            setCexTradeFee(resultOrder, direction, cexFeeRate);
        }
        return resultOrder;
    }


    /**
     * 创建CEX订单
     */
    private SpotOrderDTO cexCreateOrder(String exchangeCode, String pairCode, DirectionEnum direction, OrderTypeEnum orderType,
                                        BigDecimal price, BigDecimal volume, BigDecimal amount, boolean isReverse) {
        SpotSymbolDTO symbolDTO = cacheService.getSpotSymbol(exchangeCode, pairCode);
        // 报单检查
        if (!isReverse && !checkOrder(exchangeCode, pairCode, volume)) {
            return null;
        }
        SpotCreateOrderDTO createSpotOrder = new SpotCreateOrderDTO();
        createSpotOrder.setPairCode(pairCode);
        createSpotOrder.setDirection(direction.getCode());
        createSpotOrder.setPrice(price == null ? null : BigDecimalUtil.getBigDecimal(price, symbolDTO.getPriceScale()));
        createSpotOrder.setVolume(volume == null ? null : BigDecimalUtil.getBigDecimal(volume, symbolDTO.getVolumeScale()));
        createSpotOrder.setOrderType(orderType.getCode());
        if (orderType == OrderTypeEnum.LIMIT) {
            createSpotOrder.setTimeCondition(TimeConditionEnum.FOK.getCode());
        } else if (orderType == OrderTypeEnum.MARKET) {
            if (direction == DirectionEnum.BUY) {
                createSpotOrder.setVolume(BigDecimalUtil.getBigDecimal(amount, symbolDTO.getPriceScale()));
            }
            createSpotOrder.setTimeCondition(TimeConditionEnum.IOC.getCode());
        }
        // 发送CEX报单指令
        SpotCreateOrderRespDTO spotOrderResp = sendCexOrder(createSpotOrder);
        // 取CEX报单结果
        SpotOrderDTO spotOrderDo = getCexOrderResult(createSpotOrder.getPairCode(), spotOrderResp.getOrderId(), spotOrderResp.getLocalOrderId());
        if (spotOrderDo.getTradeAmount().compareTo(BigDecimal.ZERO) == 0) {
            log.info("CEX没有任何成交: {}-{}", spotOrderResp.getLocalOrderId(), JacksonUtil.toJSONStr(createSpotOrder));
            return null;
        } else {
            return spotOrderDo;
        }
    }

    /**
     * 计算CEX手续费
     */
    private void setCexTradeFee(SpotOrderDTO spotOrder, DirectionEnum direction, BigDecimal feeRate) {
        BigDecimal fee;
        if (direction == DirectionEnum.BUY) {
            // 买：手续费 = 成交数量 * 手续费率
            fee = spotOrder.getTradeVolume().multiply(feeRate);
        } else {
            // 卖：手续费 = 成交金额 * 手续费率
            fee = spotOrder.getTradeAmount().multiply(feeRate);
        }
        spotOrder.setFee(fee);
    }

    /**
     * 发送CEX报单指令
     */
    private SpotCreateOrderRespDTO sendCexOrder(SpotCreateOrderDTO spotOrder) {
        RemoteOrderRequest createOrderRequest = RemoteOrderRequest.create(spotOrder);
        RemoteOrderResponse remoteOrderResponse = remoteOrderService.createOrder(createOrderRequest);
        log.info("发送CEX报单指令:{}, Result:{}", JacksonUtil.toJSONStr(spotOrder), JacksonUtil.toJSONStr(remoteOrderResponse));
        if (remoteOrderResponse.isError()) {
            throw new BizException(remoteOrderResponse.getCode(), remoteOrderResponse.getMsg());
        } else {
            return new SpotCreateOrderRespDTO(remoteOrderResponse.getOrderId(), createOrderRequest.getLocalOrderId());
        }
    }

    /**
     * 取CEX报单结果
     */
    private SpotOrderDTO getCexOrderResult(String pairCode, String orderId, String localOrderId) {
        Long startTime = System.currentTimeMillis();
        while (true) {
            // 60s 超时时间
            if (System.currentTimeMillis() - startTime > 1 * 60 * 1000L) {
                log.error("取CEX报单结果超时:{}", localOrderId);
                throw new BizException(BizCodeEnum.BIZ_ERROR_GET_CEX_RESULT_TIMEOUT);
            }

            RemoteOrderQueryRequest request = RemoteOrderQueryRequest.builder()
                    .pairCode(pairCode)
                    .orderId(orderId)
                    .localOrderId(localOrderId)
                    .build();
            RemoteOrderQueryResponse result = remoteOrderService.getOrder(request);
            SpotOrderDTO spotOrder = null;
            if (result != null) {
                spotOrder = SpotOrderDTO.create(result);
            }
            log.info("取CEX报单结果:{}, Result:{}", localOrderId, JacksonUtil.toJSONStr(spotOrder));
            if (spotOrder != null &&
                    (spotOrder.getOrderStatus().equals(OrderStatusEnum.CANCELED.getCode()) ||
                            spotOrder.getOrderStatus().equals(OrderStatusEnum.ALL_TRADED.getCode()))) {
                return spotOrder;
            }
            // 延迟1秒查询
            ThreadUtil.sleep(1 * 1000);
        }
    }

    /**
     * 取DEX滑点
     */
    private String getDexSlippage(BigDecimal slippage) {
        return slippage.multiply(BigDecimal.valueOf(100)).toPlainString();
    }

    /**
     * 检查是否触发策略执行
     */
    private BigDecimal checkTrigger(BigDecimal buyPrice, BigDecimal sellPrice, BigDecimal triggerValue) {
        BigDecimal profit = BigDecimalUtil.divide(sellPrice.subtract(buyPrice), buyPrice);
        if (profit.compareTo(triggerValue) >= 0) {
            return profit;
        } else {
            return null;
        }
    }

    /**
     * 取成交金额（减掉手续费 或 滑点）
     */
    private BigDecimal getTradeAmt(BigDecimal total, BigDecimal feeRate) {
        return total.multiply(BigDecimal.ONE.subtract(feeRate));
    }

    /**
     * 取限价
     */
    private BigDecimal getLimitPrice(List<OrderBookItem> orderBook) {
        if (CollectionUtil.isEmpty(orderBook)) {
            return BigDecimal.ZERO;
        } else {
            return orderBook.get(orderBook.size() - 1).getPrice();
        }
    }

    /**
     * DEX预留gas费
     */
    private BigDecimal remainGasFee(String chainCode, String symbol, BigDecimal balance) {
        ChainEnum chain = ChainEnum.fromCode(chainCode);
        if (chain == ChainEnum.BSC && symbol.equals("BNB")) {
            return balance.subtract(CN_GAS_BSC_BNB);
        } else if (chain == ChainEnum.POLYGON && symbol.equals("MATIC")) {
            return balance.subtract(CN_GAS_POLYGON_MATIC);
        } else {
            return balance;
        }
    }

    /**
     * 报单检查
     */
    private boolean checkOrder(String exchangeCode, String pairCode, BigDecimal volume) {
        SpotSymbolDTO spotSymbol = cacheService.getSpotSymbol(exchangeCode, pairCode);
        BigDecimal minVolume = spotSymbol.getMinVolume();
        if (minVolume.compareTo(volume) > 0) {
            log.info("CEX成交金额小于最小值, {}-{}-{}", pairCode, minVolume, volume);
            return false;
        }
        return true;
    }

    /**
     * 远程服务初始化
     */
    private void initRemoteService(StrategyParamDTO strategy) {
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
     * 发送DEX SWAP指令
     *
     * @param retries 重试次数
     */
    public String sendDexSwap(String dexChainCode, String inSymbol, String outSymbol, BigDecimal amount,
                              BigDecimal slippage, String privateKey, String address, Integer retries) {
        OOV2TokenResp inToken = cacheService.getToken(dexChainCode, inSymbol);
        OOV2TokenResp outToken = cacheService.getToken(dexChainCode, outSymbol);
        BigDecimal gasPrice = cacheService.getGasPrice(dexChainCode);

        OOV2SwapReq request = OOV2SwapReq.builder()
                .account(address)
                .amount(amount.toPlainString())
                .chainId(ChainConst.chainIdMap.get(dexChainCode))
                .exChange(PlatformEnum.OPEN_OCEAN_V2.getCode())
                .gasPrice(gasPrice.toPlainString())
                .inTokenAddress(inToken.getAddress())
                .inTokenSymbol(inSymbol)
                .outTokenAddress(outToken.getAddress())
                .outTokenSymbol(outSymbol)
                .privateKey(privateKey)
                .slippage(getDexSlippage(slippage))
                .approved(Boolean.TRUE.toString())
                .withoutCheckBalance(Boolean.TRUE.toString())
                .build();
        OOV2SwapResp swapResult = null;
        for (int i = 1; i <= retries; i++) {
            try {
                log.info("发送DexSwap指令: {}-{}-{}-{}-{}-{}", dexChainCode,
                        inSymbol, outSymbol, request.getAmount(), request.getGasPrice(), request.getSlippage());
                OOV2BaseResponse<OOV2SwapResp> response = openOceanV2Api.swap(request);
                if (StrUtil.equals(response.getCode(), "209")) {
                    log.error("发送DexSwap指令异常: Return amount is not enough");
                    break;
                }
                swapResult = response.getSuccessData();
                break;
            } catch (Exception e) {
                log.error(String.format("发送DexSwap指令异常%s: %s", i, e.getMessage()), e);
                ThreadUtil.sleep(1000);
            }
        }
        if (swapResult != null) {
            log.info("发送DexSwap指令结果: {}-{}-{}-{}-{}-{}, Result:{}",
                    dexChainCode, inSymbol, outSymbol, request.getAmount(), request.getGasPrice(),
                    request.getSlippage(), JacksonUtil.toJSONStr(swapResult));
            return swapResult.getHash();
        } else {
            return null;
        }
    }

    /**
     * 查询Dex交易是否成功
     */
    public ChainHashStatusEnum dexTxIsSuccess(String chainCode, String txHash) {
        int count = 0;
        OOV2TransactionReceiptResp txResult;
        OOV2BaseResponse<OOV2TransactionReceiptResp> response;
        while (true) {
            try {
                OOV2TransactionReceiptReq request = OOV2TransactionReceiptReq.builder()
                        .chainId(ChainConst.chainIdMap.get(chainCode))
                        .hash(txHash)
                        .build();
                response = openOceanV2Api.getTransactionReceipt(request);
                txResult = response.getSuccessData();
                log.info("查询Dex交易是否成功: {}-{}, Result:{}", chainCode, txHash, JacksonUtil.toJSONStr(txResult));
                if (Objects.nonNull(txResult)) {
                    ChainHashStatusEnum hashStatus = ChainHashStatusEnum.fromCode(txResult.getStatus());
                    if (hashStatus == ChainHashStatusEnum.FAILURE || hashStatus == ChainHashStatusEnum.SUCCESS
                            || (hashStatus == ChainHashStatusEnum.NOT_ON_THE_CHAIN && count++ > 3)) {
                        return hashStatus;
                    }
                } else {
                    throw new BizException(BizCodeEnum.BIZ_ERROR_TX_RESULT_NULL);
                }
            } catch (Exception e) {
                log.error(String.format("查询Dex交易是否成功异常: %s", e.getMessage()), e);
            }
            // 延迟5秒查询
            ThreadUtil.sleep(5 * 1000);
        }
    }


}
