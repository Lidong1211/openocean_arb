package com.openocean.arb.client.command;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.openocean.arb.client.common.BaseCommand;
import com.openocean.arb.client.holder.InputParamHolder;
import com.openocean.arb.client.holder.JlineHolder;
import com.openocean.arb.constants.CommonConst;
import com.openocean.arb.constants.YesNoEnum;
import com.openocean.arb.util.BigDecimalUtil;
import lombok.Getter;
import org.jline.reader.Completer;
import org.jline.reader.impl.completer.StringsCompleter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Create Command
 *
 * @author lidong
 * @date 2022/3/14
 */
@Component
public class CreateCommand extends BaseCommand {
    @Getter
    private String command = "create";
    @Getter
    private String desc = "Create a new bot";
    @Getter
    private boolean enable = true;
    @Getter
    private boolean initShow = true;
    @Getter
    private Integer sort = 1;
    // prefix
    private static final String CEX_EXCHANGE_CODE_PREFIX = "Enter your ceFi connector ";
    private static final String CEX_PAIR_CODE_PREFIX = "Enter the token trading pair you would like to trade on ceFi (e.g. BNB/USDT) ";
    private static final String DEX_CHAIN_CODE_PREFIX = "Enter your deFi connector ";
    private static final String DEX_PAIR_CODE_PREFIX = "Enter the token trading pair you would like to trade on deFi (e.g. BNB/USDT) ";
    private static final String DEX_SLIPPAGE_PREFIX = "How much buffer do you want to add to the price to account for " +
            "slippage for orders on the first market (Enter 1 for 0.001) ";
    private static final String DEX_API_URL_PREFIX = "Enter the OpenOceanV2 apiUrl you would like to call on deFi (Enter empty string" +
            " for default config) ";
    private static final String PER_ORDER_AMOUNT_PREFIX = "What is the amount of quoteToken per order? ";
    private static final String TRIGGER_VALUE_PREFIX = "What is the minimum profitability for you to make a trade? (Enter 1 for 0.01) ";
    private static final String SAMPLING_PREFIX = "Is this a simulated strategy? (Y/N) ";
    private InputParamHolder.StrategyInfo strategyInfo;

    private String prefixTemp;
    private List<String> suggestListTemp;


    @Override
    public void open() {
        strategyInfo = new InputParamHolder.StrategyInfo();
        setTempAndReadLine(CEX_EXCHANGE_CODE_PREFIX, JlineHolder.exchangeList);
    }

    @Override
    public String deal(String prefix, String line) {
        if (StrUtil.equals(prefix, CEX_EXCHANGE_CODE_PREFIX) && JlineHolder.exchangeList.contains(line)) {
            // cexExchangeCode
            strategyInfo.setExchangeCode(line);
            setTempAndReadLine(CEX_PAIR_CODE_PREFIX, null);
        } else if (StrUtil.equals(prefix, CEX_PAIR_CODE_PREFIX) && checkPairCode(line)) {
            // cexPairCode
            strategyInfo.setCexPairCode(line.toUpperCase());
            setTempAndReadLine(DEX_CHAIN_CODE_PREFIX, JlineHolder.chainList);
        } else if (StrUtil.equals(prefix, DEX_CHAIN_CODE_PREFIX) && JlineHolder.chainList.contains(line)) {
            // dexChainCode
            strategyInfo.setDexChainCode(line);
            setTempAndReadLine(DEX_PAIR_CODE_PREFIX, null);
        } else if (StrUtil.equals(prefix, DEX_PAIR_CODE_PREFIX) && checkPairCode(line)) {
            // dexPairCode
            strategyInfo.setDexPairCode(line.toUpperCase());
            setTempAndReadLine(DEX_SLIPPAGE_PREFIX, null);
        } else if (StrUtil.equals(prefix, DEX_SLIPPAGE_PREFIX) && checkNumber(line)) {
            // dexSlippage
            String dexSlippage = BigDecimalUtil.getBigDecimal(line).divide(BigDecimal.valueOf(1000L)).toPlainString();
            strategyInfo.setDexSlippage(dexSlippage);
            setTempAndReadLine(DEX_API_URL_PREFIX, null);
        } else if (StrUtil.equals(prefix, DEX_API_URL_PREFIX)) {
            // dexApiUrl
            strategyInfo.setDexApiUrl(line);
            setTempAndReadLine(PER_ORDER_AMOUNT_PREFIX, null);
        } else if (StrUtil.equals(prefix, PER_ORDER_AMOUNT_PREFIX) && checkNumber(line)) {
            // perOrderAmount
            strategyInfo.setPerOrderAmount(line);
            setTempAndReadLine(TRIGGER_VALUE_PREFIX, null);
        } else if (StrUtil.equals(prefix, TRIGGER_VALUE_PREFIX) && checkNumber(line)) {
            // triggerValue
            String triggerValue = BigDecimalUtil.getBigDecimal(line).divide(BigDecimal.valueOf(100L)).toPlainString();
            strategyInfo.setTriggerValue(triggerValue);
            setTempAndReadLine(SAMPLING_PREFIX, null);
        } else if (StrUtil.equals(prefix, SAMPLING_PREFIX) && StrUtil.equalsAny(line, CommonConst.Y_UPPER_STR, CommonConst.N_UPPER_STR)) {
            // sampling
            strategyInfo.setSampling(StrUtil.equals(CommonConst.Y_UPPER_STR, line) ? YesNoEnum.YES.getCode() : YesNoEnum.NO.getCode());
            InputParamHolder.strategyInfo = strategyInfo;
            // 退出命令
            return super.deal(null, null);
        } else {
            readTempLine(line);
        }
        return null;
    }

    /**
     * 设置缓存,并开启命令行交互
     */
    private void setTempAndReadLine(String prefix, List<String> suggests) {
        this.prefixTemp = prefix;
        this.suggestListTemp = suggests;
        readTempLine(StrUtil.EMPTY);
    }

    /**
     * 使用缓存参数 命令行交互
     */
    private void readTempLine(String line) {
        String message = StrUtil.EMPTY;
        Completer completer = null;
        if (CollectionUtil.isNotEmpty(suggestListTemp)) {
            if (StrUtil.isNotBlank(line)) {
                message = invalidMsg(line, suggestListTemp);
            }
            completer = new StringsCompleter(suggestListTemp);
        }
        readLine(message, prefixTemp, completer);
    }

    /**
     * 数字验证
     */
    private boolean checkNumber(String numStr) {
        try {
            BigDecimal num = BigDecimalUtil.getBigDecimal(numStr);
            return num.compareTo(BigDecimal.ZERO) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证交易对
     */
    private boolean checkPairCode(String pairCode) {
        return pairCode.split(CommonConst.SLASH_STR).length == 2;
    }
}
