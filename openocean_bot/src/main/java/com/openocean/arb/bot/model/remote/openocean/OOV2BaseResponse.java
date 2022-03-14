package com.openocean.arb.bot.model.remote.openocean;

import com.openocean.arb.common.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * OpenOceanV2 base response
 *
 * @author lidong
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OOV2BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = 8981879586773088995L;

    private String code;

    private String error;

    private T data;

    /**
     * 是否为错误类型的响应
     *
     * @return 匹配结果
     */
    public boolean isError() {
        return !"200" .equals(code);
    }

    /**
     * 获取正确响应数据，否则抛出异常
     */
    public T getSuccessData() {
        if (isError()) {
            throw new BizException(String.valueOf(error));
        } else {
            return data;
        }
    }
}
