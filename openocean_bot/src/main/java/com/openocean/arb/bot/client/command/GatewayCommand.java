package com.openocean.arb.bot.client.command;

import com.openocean.arb.bot.client.common.BaseCommand;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Gateway Command
 *
 * @author lidong
 * @date 2022/3/14
 */
@Component
public class GatewayCommand extends BaseCommand {

    @Getter
    private String command = "gateway";
    @Getter
    private String desc = "Gateway API configuration";

    @Override
    public void open() {
        // todo
    }
}
