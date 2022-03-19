package com.openocean.arb.bot.client.command;

import com.openocean.arb.bot.client.common.BaseCommand;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Export Command
 *
 * @author lidong
 * @date 2022/3/14
 */
@Component
public class ExportCommand extends BaseCommand {
    @Getter
    private String command = "export";
    @Getter
    private String desc = "Export secure information";

    @Override
    public void open() {
        // todo
    }
}
