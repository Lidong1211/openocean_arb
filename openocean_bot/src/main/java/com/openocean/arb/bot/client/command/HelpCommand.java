package com.openocean.arb.bot.client.command;

import cn.hutool.core.collection.CollectionUtil;
import com.openocean.arb.bot.client.common.BaseCommand;
import com.openocean.arb.bot.client.holder.JlineHolder;
import com.openocean.arb.common.constants.CommonConst;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Help Command
 *
 * @author lidong
 * @date 2022/3/14
 */
@Component
public class HelpCommand extends BaseCommand {
    @Getter
    private String command = "help";
    @Getter
    private String desc = "List available commands";
    @Getter
    private boolean enable = true;
    @Getter
    private boolean initShow = true;
    @Getter
    private Integer sort = 2;

    @Override
    public void open() {
        // 组装提示信息
        List<String> commandHelpInfo = JlineHolder.commandList.parallelStream()
                .sorted(Comparator.comparing(BaseCommand::getSort))
                .map(BaseCommand::helpInfo).collect(Collectors.toList());
        commandHelpInfo.add(0, "Commands:");
        String message = CollectionUtil.join(commandHelpInfo, CommonConst.NEWLINE_STR);
        // 命令行交互
        readLine(message);
    }
}
