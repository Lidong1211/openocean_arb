package com.openocean.arb.client.command;

import com.openocean.arb.client.common.BaseCommand;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Rate Command
 *
 * @author lidong
 * @date 2022/3/14
 */
@Component
public class RateCommand extends BaseCommand {
    @Getter
    private String command = "rate";
    @Getter
    private String desc = "Show rate of a given trading pair";


    @Override
    public void open() {
        // todo
    }
}
