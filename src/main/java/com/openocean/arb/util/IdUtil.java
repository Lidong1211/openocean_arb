package com.openocean.arb.util;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.experimental.UtilityClass;

/**
 * ID工具类
 **/
@UtilityClass
public class IdUtil {

    /**
     * 生成本地订单编号：HEX（时间戳）+ 6位随机数
     *
     * @return 17位字符串，前11位递增，后6位随机
     */
    public String genLocalOrderId() {
        return HexUtil.toHex(System.currentTimeMillis()) + RandomUtil.randomString(6);
    }

}
