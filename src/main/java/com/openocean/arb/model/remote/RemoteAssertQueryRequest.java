package com.openocean.arb.model.remote;

import lombok.Builder;
import lombok.Data;

/**
 * 远程资产查询请求
 *
 * @author lidong
 */
@Data
@Builder
public class RemoteAssertQueryRequest {
    // 交易对：/分割
    private String pairCode;
}
