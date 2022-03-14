package com.openocean.arb.bot.controller;

import com.openocean.arb.bot.service.StrategyService;
import com.openocean.arb.common.util.BotUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 策略启动接口（暂时使用接口开启）
 *
 * @author lidong
 * @date 2022/3/10
 */
@RestController
public class StrategyController {

    @Autowired
    private StrategyService strategyService;

    @GetMapping("/start")
    public String  start() {
        strategyService.startStrategy();
        return "ok";
    }

    @GetMapping("/stop")
    public String stop() {
        BotUtil.stop();
        return "ok";
    }
}
