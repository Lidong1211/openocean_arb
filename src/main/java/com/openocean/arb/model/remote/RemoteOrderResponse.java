package com.openocean.arb.model.remote;

import com.openocean.arb.constants.BizCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 远程报单响应
 *
 * @author lidong
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoteOrderResponse {
    // 代码
    private Integer code;
    // 信息
    private String msg;
    // 订单编号
    private String orderId;

    public boolean isError() {
        return !BizCodeEnum.BIZ_SUCCESS.getCode().equals(code);
    }
}
