package com.openocean.arb.bot.config.remote.openocean;

import com.openocean.arb.bot.model.remote.openocean.*;
import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

import java.util.List;


@Headers({"Connection: close", "Content-Type: application/json"})
public interface OpenOceanV2Api {

    @RequestLine("GET /v1/cross/quote")
    OOV2BaseResponse<OOV2QuoteResp> getQuote(@QueryMap OOV2QuoteReq req);

    @RequestLine("GET /v1/cross/tokenList")
    OOV2BaseResponse<List<OOV2TokenResp>> tokenList(@QueryMap OOV2TokenReq req);

    @RequestLine("GET /v1/cross/getBalance")
    OOV2BaseResponse<List<OOV2BalanceResp>> getBalance(@QueryMap OOV2BalanceReq req);

    @RequestLine("GET /v1/cross/swap")
    OOV2BaseResponse<OOV2SwapResp> swap(@QueryMap OOV2SwapReq req);

    @RequestLine("GET /v1/cross/getTransaction")
    OOV2BaseResponse<OOV2TransactionResp> getTransaction(@QueryMap OOV2TransactionReq req);

    @RequestLine("GET /v1/cross/getTransactionReceipt")
    OOV2BaseResponse<OOV2TransactionReceiptResp> getTransactionReceipt(@QueryMap OOV2TransactionReceiptReq req);

    @RequestLine("GET /v1/{chainId}/getGasPrice")
    OOV2BaseResponse<OOV2GasPriceResp> getGasPrice(@Param("chainId") String chainId);

    @RequestLine("GET /v1/cross/approve")
    OOV2BaseResponse approve(@QueryMap OOV2ApproveReq req);

    @RequestLine("GET /v1/cross/allowance")
    OOV2BaseResponse<OOV2AllowanceResp> allowance(@QueryMap OOV2ApproveReq req);

}
