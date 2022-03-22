package com.openocean.arb.util;

import com.openocean.arb.constants.BizCodeEnum;
import com.openocean.arb.exception.BizException;
import lombok.experimental.UtilityClass;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class BotUtil {

    public static ApplicationContext applicationContext;

    /**
     * 退出系统
     */
    public static void exit() {
        int exitCode = SpringApplication.exit(BotUtil.applicationContext, (ExitCodeGenerator) () -> 0);
        System.exit(exitCode);
    }

    /**
     * 触发但未成功完成交易次数
     * 连续失败3次则直接暂停
     */
    private static final AtomicInteger TRIGGER_NOT_SUCCESS_COUNT = new AtomicInteger(0);

    private static Boolean isStop = true;

    public static void stop() {
        isStop = true;
    }

    public static void start() {
        isStop = false;
    }

    public static Boolean isStop() {
        return isStop;
    }

    /**
     * 触发次数增加
     */
    public static void triggerAdd() {
        int count = TRIGGER_NOT_SUCCESS_COUNT.incrementAndGet();
        if (count > 3) {
            stop();
            throw new BizException(BizCodeEnum.BIZ_ERROR);
        }
    }

    /**
     * 触发次数清零
     */
    public static void triggerClear() {
        TRIGGER_NOT_SUCCESS_COUNT.set(0);
    }
}
