package com.openocean.arb.client.holder;

import com.openocean.arb.client.common.BaseCommand;
import com.openocean.arb.client.common.InitCommand;
import com.openocean.arb.service.remote.RemoteOrderService;
import com.openocean.arb.constants.ChainConst;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Jline 交互中一些配置信息
 *
 * @author lidong
 * @date 2022/3/16
 */
public class JlineHolder {

    /**
     * command
     */
    public static List<BaseCommand> commandList;
    public static Map<String, BaseCommand> commandMap;
    public static List<String> commandStrList;
    public static InitCommand initCommand;

    /**
     * CeFi remoteService
     */
    public static List<RemoteOrderService> remoteOrderServices;
    public static Map<String, RemoteOrderService> remoteOrderServiceMap;
    public static List<String> exchangeList;

    /**
     * DeFi chain
     */
    public static List<String> chainList = ChainConst.chainIdMap.keySet().parallelStream().collect(Collectors.toList());


}
