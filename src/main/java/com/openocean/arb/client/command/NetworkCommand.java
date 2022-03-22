package com.openocean.arb.client.command;

import cn.hutool.core.util.StrUtil;
import com.openocean.arb.client.common.BaseCommand;
import com.openocean.arb.client.holder.InputParamHolder;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Network Command
 *
 * @author lidong
 * @date 2022/3/22
 */
@Component
public class NetworkCommand extends BaseCommand {

    @Getter
    private String command = "network";
    @Getter
    private String desc = "Set your network configuration";
    @Getter
    private boolean enable = true;
    @Getter
    private boolean initShow = true;
    @Getter
    private Integer sort = 2;
    private InputParamHolder.NetworkProxy networkProxy;

    private static final String PROXY_HOST_PREFIX = "Enter your network proxy host (e.g. 10.17.145.201)";
    private static final String PROXY_PORT_PREFIX = "Enter your network proxy port (e.g. 1080)";

    @Override
    public void open() {
        networkProxy = new InputParamHolder.NetworkProxy();
        readLine(StrUtil.EMPTY, PROXY_HOST_PREFIX, null);
    }

    @Override
    public String deal(String prefix, String line) {
        if (StrUtil.equals(prefix, PROXY_HOST_PREFIX)) {
            if (StrUtil.isBlank(line)) {
                readLine(StrUtil.EMPTY, PROXY_HOST_PREFIX, null);
            } else {
                networkProxy.setHost(line);
                readLine(StrUtil.EMPTY, PROXY_PORT_PREFIX, null);
            }
        } else if (StrUtil.equals(prefix, PROXY_PORT_PREFIX)) {
            if (StrUtil.isBlank(line)) {
                readLine(StrUtil.EMPTY, PROXY_PORT_PREFIX, null);
            } else {
                networkProxy.setPort(line);
                InputParamHolder.networkProxy = networkProxy;
                return super.deal(null, null);
            }
        }
        return null;
    }
}

