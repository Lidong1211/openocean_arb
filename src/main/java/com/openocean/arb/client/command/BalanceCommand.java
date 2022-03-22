package com.openocean.arb.client.command;

import com.openocean.arb.client.common.BaseCommand;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Balance Command
 *
 * @author lidong
 * @date 2022/3/14
 */
@Component
public class BalanceCommand extends BaseCommand {

    @Getter
    private String command = "balance";
    @Getter
    private String desc = "Display your asset balances across all connected exchanges";

    @Override
    public void open() {
        // todo
    }
}
