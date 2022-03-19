import com.openocean.arb.bot.Application;
import com.openocean.arb.bot.service.cache.CacheService;
import com.openocean.arb.bot.config.remote.openocean.OpenOceanV2Api;
import com.openocean.arb.bot.model.remote.openocean.*;
import com.openocean.arb.common.constants.ChainConst;
import com.openocean.arb.common.constants.PlatformEnum;
import com.openocean.arb.common.util.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DeFi 接口单元测试
 *
 * @author lidong
 * @date 2022/3/12
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class OpenOceanV2Test {
    @Autowired
    private OpenOceanV2Api openOceanV2Api;
    @Autowired
    private CacheService cacheService;
    // bsc、polygon
    private static final String CHAIN_CODE = "bsc";
    private static final String DEX_ACCOUNT = "";
    private static final String DEX_PRIVATE_KEY = "";


    /**
     * 获取报价
     */
    @Test
    public void getQuote() {
        String inSymbol = "SXP";
        String outSymbol = "USDT";
        String amount = "100";
        String dexSlippage = "3";
        OOV2TokenResp inToken = cacheService.getToken(CHAIN_CODE, inSymbol);
        OOV2TokenResp outToken = cacheService.getToken(CHAIN_CODE, outSymbol);
        BigDecimal gasPrice = cacheService.getGasPrice(CHAIN_CODE);
        OOV2QuoteReq request = OOV2QuoteReq.builder()
                .inTokenSymbol(inSymbol)
                .inTokenAddress(inToken.getAddress())
                .outTokenSymbol(outSymbol)
                .outTokenAddress(outToken.getAddress())
                .chainId(ChainConst.chainIdMap.get(CHAIN_CODE))
                .exChange(PlatformEnum.OPEN_OCEAN_V2.getCode())
                .amount(amount)
                .gasPrice(gasPrice.toPlainString())
                .slippage(dexSlippage)
                .build();
        OOV2BaseResponse<OOV2QuoteResp> response = openOceanV2Api.getQuote(request);
        log.info("获取报价 request={}", JacksonUtil.toJSONStr(request));
        log.info("获取报价 response={}", JacksonUtil.toJSONStr(response.getSuccessData()));
    }

    /**
     * 获取链上Token List
     */
    @Test
    public void tokenList() {
        OOV2TokenReq request = OOV2TokenReq.builder()
                .chainId(ChainConst.chainIdMap.get(CHAIN_CODE))
                .build();
        OOV2BaseResponse<List<OOV2TokenResp>> response = openOceanV2Api.tokenList(request);
        log.info("获取链上Token List request={}", JacksonUtil.toJSONStr(request));
        log.info("获取链上Token List response={}", JacksonUtil.toJSONStr(response.getSuccessData()));
    }

    /**
     * 获取链上钱包账户余额
     */
    @Test
    public void getBalance() {
        List<String> symbols = Lists.list("BNB", "USDT", "SXP");
        List<String> addressList = symbols.stream().map(s -> cacheService.getToken(CHAIN_CODE, s).getAddress()).collect(Collectors.toList());
        OOV2BalanceReq request = OOV2BalanceReq.builder()
                .chainId(ChainConst.chainIdMap.get(CHAIN_CODE))
                .inTokenAddress(String.join(",", addressList))
                .account(DEX_ACCOUNT)
                .build();
        OOV2BaseResponse<List<OOV2BalanceResp>> response = openOceanV2Api.getBalance(request);
        log.info("获取链上钱包账户余额 request={}", JacksonUtil.toJSONStr(request));
        log.info("获取链上钱包账户余额 response={}", JacksonUtil.toJSONStr(response.getSuccessData()));
    }

    /**
     * 发起链上 Swap
     */
    @Test
    public void swap() {
        String inSymbol = "USDT";
        String outSymbol = "BNB";
        String amount = "1";
        String dexSlippage = "0.5";
        BigDecimal gasPrice = cacheService.getGasPrice(CHAIN_CODE);
        OOV2TokenResp inToken = cacheService.getToken(CHAIN_CODE, inSymbol);
        OOV2TokenResp outToken = cacheService.getToken(CHAIN_CODE, outSymbol);
        OOV2SwapReq request = OOV2SwapReq.builder()
                .chainId(ChainConst.chainIdMap.get(CHAIN_CODE))
                .exChange(PlatformEnum.OPEN_OCEAN_V2.getCode())
                .withoutCheckBalance(Boolean.TRUE.toString())
                .gasPrice(gasPrice.toPlainString())
                .approved(Boolean.TRUE.toString())
                .privateKey(DEX_PRIVATE_KEY)
                .account(DEX_ACCOUNT)
                .inTokenSymbol(inSymbol)
                .inTokenAddress(inToken.getAddress())
                .outTokenSymbol(outSymbol)
                .outTokenAddress(outToken.getAddress())
                .slippage(dexSlippage)
                .amount(amount)
                .build();
        OOV2BaseResponse<OOV2SwapResp> response = openOceanV2Api.swap(request);
        log.info("发起链上 Swap request={}", JacksonUtil.toJSONStr(request));
        log.info("发起链上 Swap response={}", JacksonUtil.toJSONStr(response.getSuccessData()));
    }

    /**
     * 获取链上交易信息
     */
    @Test
    public void getTransaction() {
        String txHash = "0xdd11ba7e6fbf09959791efbc177b7cfba4c78dafc34fc8e46a5ae920edec216c";
        OOV2TransactionReq request = OOV2TransactionReq.builder()
                .chainId(ChainConst.chainIdMap.get(CHAIN_CODE))
                .exChange(PlatformEnum.OPEN_OCEAN_V2.getCode())
                .hash(txHash)
                .type("swap")
                .build();
        OOV2BaseResponse<OOV2TransactionResp> response = openOceanV2Api.getTransaction(request);
        log.info("获取链上交易信息 request={}", JacksonUtil.toJSONStr(request));
        log.info("获取链上交易信息 response={}", JacksonUtil.toJSONStr(response.getSuccessData()));
    }

    /**
     * 获取链上交易状态
     */
    @Test
    public void getTransactionReceipt() {
        String txHash = "0xdd11ba7e6fbf09959791efbc177b7cfba4c78dafc34fc8e46a5ae920edec216c";
        OOV2TransactionReceiptReq request = OOV2TransactionReceiptReq.builder()
                .chainId(ChainConst.chainIdMap.get(CHAIN_CODE))
                .hash(txHash)
                .build();
        OOV2BaseResponse<OOV2TransactionReceiptResp> response = openOceanV2Api.getTransactionReceipt(request);
        log.info("获取链上交易状态 request={}", JacksonUtil.toJSONStr(request));
        log.info("获取链上交易状态 response={}", JacksonUtil.toJSONStr(response.getSuccessData()));
    }

    /**
     * 获取链上GasPrice
     */
    @Test
    public void getGasPrice() {
        String chainId = ChainConst.chainIdMap.get(CHAIN_CODE);
        OOV2BaseResponse<OOV2GasPriceResp> response = openOceanV2Api.getGasPrice(chainId);
        log.info("获取链上GasPrice {}-{}", CHAIN_CODE, response.getData().getGasPrice());
    }

    /**
     * 授权链上SWAP交易
     */
    @Test
    public void approve() {
        String symbol = "USDT";
        OOV2TokenResp token = cacheService.getToken(CHAIN_CODE, symbol);
        OOV2ApproveReq request = OOV2ApproveReq.builder()
                .exChange(PlatformEnum.OPEN_OCEAN_V2.getCode())
                .chainId(ChainConst.chainIdMap.get(CHAIN_CODE))
                .inTokenAddress(token.getAddress())
                .privateKey(DEX_PRIVATE_KEY)
                .account(DEX_ACCOUNT)
                .amount("1")
                .build();
        OOV2BaseResponse response = openOceanV2Api.approve(request);
        log.info("授权链上SWAP交易 request={}", JacksonUtil.toJSONStr(request));
        log.info("授权链上SWAP交易 response={}", JacksonUtil.toJSONStr(response.getSuccessData()));
    }

    /**
     * 获取链上SWAP交易授权状态
     */
    @Test
    public void allowance() {
        String symbol = "USDT";
        OOV2TokenResp token = cacheService.getToken(CHAIN_CODE, symbol);
        OOV2ApproveReq request = OOV2ApproveReq.builder()
                .exChange(PlatformEnum.OPEN_OCEAN_V2.getCode())
                .chainId(ChainConst.chainIdMap.get(CHAIN_CODE))
                .inTokenAddress(token.getAddress())
                .privateKey(DEX_PRIVATE_KEY)
                .account(DEX_ACCOUNT)
                .amount("1")
                .build();
        OOV2BaseResponse<OOV2AllowanceResp> response = openOceanV2Api.allowance(request);
        log.info("获取链上SWAP交易授权状态 request={}", JacksonUtil.toJSONStr(request));
        log.info("获取链上SWAP交易授权状态 response={}", JacksonUtil.toJSONStr(response.getSuccessData()));
    }

}
