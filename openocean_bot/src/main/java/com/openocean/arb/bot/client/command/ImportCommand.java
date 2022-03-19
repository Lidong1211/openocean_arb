package com.openocean.arb.bot.client.command;

import com.openocean.arb.bot.client.common.BaseCommand;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Import Command
 *
 * @author lidong
 * @date 2022/3/14
 */
@Component
public class ImportCommand extends BaseCommand {
    @Getter
    private String command = "import";
    @Getter
    private String desc = "Import and existing bot by loading the configuration";

    @Override
    public void open() {
        // todo
    }
}
