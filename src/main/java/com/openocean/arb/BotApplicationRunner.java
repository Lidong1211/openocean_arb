package com.openocean.arb;

import cn.hutool.core.thread.ThreadUtil;
import com.openocean.arb.client.common.BaseCommand;
import com.openocean.arb.client.common.InitCommand;
import com.openocean.arb.client.holder.JlineHolder;
import com.openocean.arb.service.remote.RemoteOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 系统启动完成后执行操作
 *
 * @author lidong
 */
@Slf4j
@Component
public class BotApplicationRunner implements ApplicationRunner {

    @Autowired
    private InitCommand initCommand;
    @Autowired
    List<BaseCommand> commandList;
    @Autowired
    List<RemoteOrderService> remoteOrderServices;

    @Override
    public void run(ApplicationArguments args) {
        // command
        JlineHolder.commandList = commandList.parallelStream().filter(BaseCommand::isEnable).collect(Collectors.toList());
        JlineHolder.commandStrList = JlineHolder.commandList.parallelStream().map(BaseCommand::getCommand).collect(Collectors.toList());
        JlineHolder.commandMap = JlineHolder.commandList.parallelStream().collect(Collectors.toMap(BaseCommand::getCommand, Function.identity(), (s1, s2) -> s2));
        JlineHolder.initCommand = initCommand;
        // remoteOrderService
        JlineHolder.remoteOrderServices = remoteOrderServices.parallelStream().filter(RemoteOrderService::getEnabled).collect(Collectors.toList());
        JlineHolder.exchangeList = JlineHolder.remoteOrderServices.parallelStream().map(RemoteOrderService::getExchangeCode).collect(Collectors.toList());
        JlineHolder.remoteOrderServiceMap = JlineHolder.remoteOrderServices.parallelStream().collect(Collectors.toMap(RemoteOrderService::getExchangeCode, Function.identity(), (s1, s2) -> s2));

        ThreadUtil.execute(() -> initCommand.open());
    }
}