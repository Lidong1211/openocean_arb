package com.openocean.arb.bot.client.command;

import com.openocean.arb.bot.client.common.BaseCommand;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Status Command
 *
 * @author lidong
 * @date 2022/3/14
 */
@Component
public class StatusCommand extends BaseCommand {
    @Getter
    private String command = "status";
    @Getter
    private String desc = "Get the market status of the current bot";

    @Override
    public void open() {
        // todo
    }
}
