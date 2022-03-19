package com.openocean.arb.bot.client.command;

import com.openocean.arb.bot.client.common.BaseCommand;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * OrderBook Command
 *
 * @author lidong
 * @date 2022/3/14
 */
@Component
public class OrderBookCommand extends BaseCommand {
    @Getter
    private String command = "order_book";
    @Getter
    private String desc = "Display current order book";

    @Override
    public void open() {
        // todo
    }
}
