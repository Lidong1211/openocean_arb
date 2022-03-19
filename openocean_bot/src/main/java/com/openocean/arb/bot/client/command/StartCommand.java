package com.openocean.arb.bot.client.command;

import com.openocean.arb.bot.client.common.BaseCommand;
import com.openocean.arb.bot.service.StrategyService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Start Command
 *
 * @author lidong
 * @date 2022/3/14
 */
@Component
public class StartCommand extends BaseCommand {
    @Getter
    private String command = "start";
    @Getter
    private String desc = "Start the current bot";
    @Getter
    private boolean enable = true;
    @Autowired
    private StrategyService strategyService;

    @Override
    public void open() {
        // 组装提示信息
        String message = "The bot is starting ...";
        try {
            strategyService.startStrategy();
        } catch (Exception e) {
            message = e.getMessage();
        }
        // 命令行交互
        readLine(message);
    }

}
