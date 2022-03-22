package com.openocean.arb.util;

import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * BigDecimal工具类
 **/
@UtilityClass
public class BigDecimalUtil {

    public BigDecimal getBigDecimal(BigDecimal val, int scale) {
        return val == null || val.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : val.setScale(scale, BigDecimal.ROUND_DOWN);
    }

    public BigDecimal getBigDecimal(String val, int scale) {
        return StrUtil.isBlank(val) ? BigDecimal.ZERO : getBigDecimal(new BigDecimal(val), scale);
    }

    public String getString(BigDecimal val, int scale) {
        return getBigDecimal(val, scale).toPlainString();
    }

    public String getString(BigDecimal val) {
        return val == null ? BigDecimal.ZERO.toString() : val.toPlainString();
    }

    public String getString(String val, int scale) {
        return getBigDecimal(val, scale).toPlainString();
    }

    public BigDecimal getBigDecimal(String val) {
        return StrUtil.isBlank(val) ? BigDecimal.ZERO : new BigDecimal(val);
    }

    public BigDecimal getInverse(BigDecimal val) {
        return val == null || val.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : BigDecimal.ONE.divide(val, 10, BigDecimal.ROUND_DOWN);
    }

    public BigDecimal getInverse(String val) {
        return getInverse(getBigDecimal(val));
    }

    public BigDecimal getManualAmount(String amount, Integer decimals) {
        return getManualAmount(new BigDecimal(amount), decimals);
    }

    public BigDecimal getManualAmount(BigDecimal amount, Integer decimals) {
        return amount.divide(new BigDecimal(10).pow(decimals), 10, BigDecimal.ROUND_DOWN);
    }

    public BigDecimal getMin(BigDecimal bd1, BigDecimal bd2) {
        return bd1.compareTo(bd2) < 0 ? bd1 : bd2;
    }

    public BigDecimal getMax(BigDecimal bd1, BigDecimal bd2) {
        return bd1.compareTo(bd2) > 0 ? bd1 : bd2;
    }

    public BigDecimal divide(BigDecimal bd1, BigDecimal bd2) {
        return divide(bd1, bd2, 8);
    }

    public BigDecimal divide(BigDecimal bd1, BigDecimal bd2, int scale) {
        if (bd2.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        } else {
            return bd1.divide(bd2, scale, BigDecimal.ROUND_DOWN);
        }
    }

    public BigDecimal getDefaultValue(BigDecimal bd) {
        return getDefaultValue(bd, BigDecimal.ZERO);
    }


    public BigDecimal getDefaultValue(BigDecimal bd, BigDecimal defaultValue) {
        return Objects.isNull(bd) ? defaultValue : bd;
    }

}
