package com.openocean.arb.bot.client.common;

import cn.hutool.core.collection.CollectionUtil;
import com.openocean.arb.bot.client.holder.JlineHolder;
import com.openocean.arb.common.constants.CommonConst;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 初始页面
 *
 * @author lidong
 * @date 2022/3/14
 */
@Component
public class InitCommand extends BaseCommand {
    List<String> initMessages = CollectionUtil.newArrayList(
            "=================================================================================",
            "Useful Commands:");

    public void open() {
        // 组装初始页面提示信息
        List<String> commandHelpInfos = JlineHolder.commandList.parallelStream().filter(BaseCommand::isInitShow)
                .sorted(Comparator.comparing(BaseCommand::getSort))
                .map(BaseCommand::helpInfo).collect(Collectors.toList());
        initMessages.addAll(commandHelpInfos);
        String initMsg = CollectionUtil.join(initMessages, CommonConst.NEWLINE_STR);
        // 命令行交互
        readLine(initMsg);
    }

}
