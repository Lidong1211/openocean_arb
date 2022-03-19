package com.openocean.arb.bot.client.command;

import com.openocean.arb.bot.client.common.BaseCommand;
import com.openocean.arb.bot.service.StrategyService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Stop Command
 *
 * @author lidong
 * @date 2022/3/14
 */
@Component
public class StopCommand extends BaseCommand {
    @Getter
    private String command = "stop";
    @Getter
    private String desc = "Stop the current bot";
    @Getter
    private boolean enable = true;

    @Autowired
    private StrategyService strategyService;

    @Override
    public void open() {
        // 组装提示信息
        String message = "The bot is stopping ...";
        try {
            strategyService.stopStrategy();
        } catch (Exception e) {
            message = e.getMessage();
        }
        // 命令行交互
        readLine(message);
    }

}
