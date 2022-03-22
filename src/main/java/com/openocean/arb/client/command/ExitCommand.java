package com.openocean.arb.client.command;

import com.openocean.arb.client.common.BaseCommand;
import com.openocean.arb.util.BotUtil;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Exit Command
 *
 * @author lidong
 * @date 2022/3/14
 */
@Component
public class ExitCommand extends BaseCommand {
    @Getter
    private String command = "exit";
    @Getter
    private String desc = "Exit and cancel all outstanding orders";
    @Getter
    private boolean enable = true;

    @Override
    public void open() {
        BotUtil.exit();
    }

}
