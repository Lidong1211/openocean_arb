package com.openocean.arb.bot.client.command;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.openocean.arb.bot.client.common.BaseCommand;
import com.openocean.arb.bot.client.holder.InputParamHolder;
import com.openocean.arb.bot.client.holder.JlineHolder;
import com.openocean.arb.common.constants.CommonConst;
import lombok.Getter;
import org.assertj.core.util.Lists;
import org.jline.reader.Completer;
import org.jline.reader.impl.completer.StringsCompleter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Connect Command
 *
 * @author lidong
 * @date 2022/3/14
 */
@Component
public class ConnectCommand extends BaseCommand {
    @Getter
    private String command = "connect";
    @Getter
    private String desc = "Config available ceFi/deFi connect info";
    @Getter
    private boolean enable = true;
    @Getter
    private boolean initShow = true;
    @Getter
    private Integer sort = 0;
    private static final List<String> EXCHANGE_TYPE_LIST = Lists.newArrayList("cex", "dex");
    public static final String EXCHANGE_TYPE_PREFIX = "Enter the exchangeType you want to connect ";
    public static final String CEX_EXCHANGE_CODE_PREFIX = "Enter the exchange you want to connect ";
    public static final String CEX_API_KEY_PREFIX = "Enter your ceFi API key ";
    public static final String CEX_API_SECRET_PREFIX = "Enter your ceFi API secret ";
    public static final String DEX_CHAIN_CODE_PREFIX = "Enter the chain you want to connect ";
    public static final String DEX_ADDRESS_PREFIX = "Enter your deFi wallet address ";
    public static final String DEX_PRIVATE_KEY_PREFIX = "Enter your deFi wallet privateKey ";
    public InputParamHolder.CexConnectInfo cexConnectInfo;
    public InputParamHolder.DexConnectInfo dexConnectInfo;

    private String prefixTemp;
    private List<String> suggestListTemp;

    @Override
    public void open() {
        setTempAndReadLine(EXCHANGE_TYPE_PREFIX, EXCHANGE_TYPE_LIST);
    }

    @Override
    public String deal(String prefix, String line) {
        if (StrUtil.equals(prefix, EXCHANGE_TYPE_PREFIX) && EXCHANGE_TYPE_LIST.contains(line)) {
            if (StrUtil.equals(line, "cex")) {
                cexConnectInfo = new InputParamHolder().new CexConnectInfo();
                setTempAndReadLine(CEX_EXCHANGE_CODE_PREFIX, JlineHolder.exchangeList);
            } else if (StrUtil.equals(line, "dex")) {
                dexConnectInfo = new InputParamHolder().new DexConnectInfo();
                setTempAndReadLine(DEX_CHAIN_CODE_PREFIX, JlineHolder.chainList);
            }

            // CeFi相关
        } else if (StrUtil.equals(prefix, CEX_EXCHANGE_CODE_PREFIX) && JlineHolder.exchangeList.contains(line)) {
            cexConnectInfo.setExchangeCode(line);
            setTempAndReadLine(CEX_API_KEY_PREFIX, null);
        } else if (StrUtil.equals(prefix, CEX_API_KEY_PREFIX) && StrUtil.isNotBlank(line)) {
            cexConnectInfo.setApiKey(line);
            setTempAndReadLine(CEX_API_SECRET_PREFIX, null);
        } else if (StrUtil.equals(prefix, CEX_API_SECRET_PREFIX) && StrUtil.isNotBlank(line)) {
            cexConnectInfo.setApiSecret(line);
            InputParamHolder.cexConnectMap.put(cexConnectInfo.getExchangeCode(), cexConnectInfo);
            cexConnectInfo = null;
            super.deal(null, null);

            // DeFi 相关
        } else if (StrUtil.equals(prefix, DEX_CHAIN_CODE_PREFIX) && JlineHolder.chainList.contains(line)) {
            dexConnectInfo.setChainCode(line);
            setTempAndReadLine(DEX_ADDRESS_PREFIX, null);
        } else if (StrUtil.equals(prefix, DEX_ADDRESS_PREFIX) && StrUtil.isNotBlank(line)) {
            dexConnectInfo.setAddress(line);
            setTempAndReadLine(DEX_PRIVATE_KEY_PREFIX, null);
        } else if (StrUtil.equals(prefix, DEX_PRIVATE_KEY_PREFIX) && StrUtil.isNotBlank(line)) {
            dexConnectInfo.setPrivateKey(line);
            InputParamHolder.dexConnectMap.put(dexConnectInfo.getChainCode(), dexConnectInfo);
            dexConnectInfo = null;
            super.deal(null, null);
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
        Character mask = StrUtil.equalsAny(prefixTemp, CEX_API_SECRET_PREFIX, DEX_PRIVATE_KEY_PREFIX) ? CommonConst.ASTERISK_CHAR : null;
        readLine(message, prefixTemp, completer, mask);
    }

}
