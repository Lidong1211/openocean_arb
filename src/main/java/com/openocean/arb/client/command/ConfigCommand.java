package com.openocean.arb.client.command;

import cn.hutool.core.util.StrUtil;
import com.openocean.arb.client.common.BaseCommand;
import com.openocean.arb.client.holder.InputParamHolder;
import com.openocean.arb.client.holder.JlineHolder;
import com.openocean.arb.constants.CommonConst;
import com.openocean.arb.util.JacksonUtil;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Config Command
 *
 * @author lidong
 * @date 2022/3/14
 */
@Component
public class ConfigCommand extends BaseCommand {
    @Getter
    private String command = "config";
    @Getter
    private String desc = "Display the current bot's configuration";
    @Getter
    protected boolean enable = true;

    public static final String CROSS_LINE = StrUtil.padPre(StrUtil.EMPTY, 50, CommonConst.EQUALS_STR);

    @Override
    public void open() {
        List<String> lineMsgList = Lists.newArrayList();
        // 策略信息
        lineMsgList.add(CROSS_LINE);
        lineMsgList.add("Strategy config:");
        InputParamHolder.StrategyInfo strategyInfo = InputParamHolder.strategyInfo;
        strategyInfo = strategyInfo == null ? new InputParamHolder.StrategyInfo() : strategyInfo;
        Map<String, String> map = JacksonUtil.parseObject(JacksonUtil.toJSONStr(strategyInfo), Map.class);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            lineMsgList.add(dataStr(entry.getKey(), StringUtils.defaultString(entry.getValue(), "--")));
        }
        lineMsgList.add(CROSS_LINE);
        // 代理配置信息
        InputParamHolder.NetworkProxy network = InputParamHolder.networkProxy;
        String hostAndPort = network == null ? StrUtil.EMPTY : network.getHost() + CommonConst.COLON_STR + network.getPort();
        lineMsgList.add("network config:");
        lineMsgList.add(dataStr("proxy", hostAndPort));
        lineMsgList.add(CROSS_LINE);
        // CeFi
        lineMsgList.add("CeFi connect config:");
        for (String exchange : JlineHolder.exchangeList) {
            boolean contains = InputParamHolder.cexConnectMap.containsKey(exchange);
            lineMsgList.add(dataStr(exchange, contains ? "√" : "×"));
        }
        lineMsgList.add(CROSS_LINE);
        // DeFi
        lineMsgList.add("DeFi connect config:");
        for (String chain : JlineHolder.chainList) {
            boolean contains = InputParamHolder.dexConnectMap.containsKey(chain);
            lineMsgList.add(dataStr(chain, contains ? "√" : "×"));
        }

        // 命令行交互
        readLine(StrUtil.join(CommonConst.NEWLINE_STR, lineMsgList));
    }

    /**
     * 数据
     */
    private String dataStr(String left, String right) {
        return StrUtil.padPre(left, 20, CommonConst.BLANK_STR) + "    " + right;
    }

}
