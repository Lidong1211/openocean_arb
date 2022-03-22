package com.openocean.arb.client.command;

import com.openocean.arb.client.common.BaseCommand;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Ticker Command
 *
 * @author lidong
 * @date 2022/3/14
 */
@Component
public class TickerCommand extends BaseCommand {
    @Getter
    private String command = "ticker";
    @Getter
    private String desc = "Show market ticker of current order book";


    @Override
    public void open() {
        // todo
    }
}
