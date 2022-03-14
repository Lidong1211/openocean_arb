package com.openocean.arb.common.util;

import com.openocean.arb.common.constants.BizCodeEnum;
import com.openocean.arb.common.exception.BizException;
import lombok.experimental.UtilityClass;

import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class BotUtil {

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
