package com.openocean.arb.bot.client.command;

import com.openocean.arb.bot.client.common.BaseCommand;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * History Command
 *
 * @author lidong
 * @date 2022/3/14
 */
@Component
public class HistoryCommand extends BaseCommand {

    @Getter
    private String command = "history";
    @Getter
    private String desc = "See the past performance of the current bot";


    @Override
    public void open() {
        // todo
    }
}
