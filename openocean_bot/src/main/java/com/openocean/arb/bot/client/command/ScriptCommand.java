package com.openocean.arb.bot.client.command;

import com.openocean.arb.bot.client.common.BaseCommand;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Script Command
 *
 * @author lidong
 * @date 2022/3/14
 */
@Component
public class ScriptCommand extends BaseCommand {
    @Getter
    private String command = "script";
    @Getter
    private String desc = "Send command to running script instance";

    @Override
    public void open() {
        // todo
    }
}
