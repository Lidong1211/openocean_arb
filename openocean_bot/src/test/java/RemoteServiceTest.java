import cn.hutool.core.util.StrUtil;
import com.openocean.arb.bot.Application;
import com.openocean.arb.bot.model.remote.*;
import com.openocean.arb.bot.service.remote.RemoteOrderService;
import com.openocean.arb.common.util.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * CeFi 接口单元测试
 *
 * @author lidong
 * @date 2021/10/22
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class RemoteServiceTest {

    @Autowired
    List<RemoteOrderService> remoteOrderServiceList;
    public static final String EXCHANGE_CODE = "binance";
    public static final String API_KEY = "";
    public static final String API_SECRET = "";
    public static final String SPOT_ACCOUNT_ID = "";
    public static final String PASS_PHRASE = "";
    private RemoteOrderService remoteOrderService;

    @PostConstruct
    public void init() {
        if (remoteOrderServiceList != null) {
            remoteOrderService = remoteOrderServiceList.stream()
                    .filter(s -> StrUtil.equals(s.getExchangeCode(), EXCHANGE_CODE))
                    .findFirst().orElseGet(null);
        }
    }

    private void registerApi() {
        RemoteRegisterApiRequest request = RemoteRegisterApiRequest.builder()
                .apiKey(API_KEY)
                .secret(API_SECRET)
                .spotAccountId(SPOT_ACCOUNT_ID)
                .passPhrase(PASS_PHRASE)
                .build();
        remoteOrderService.registerApi(request);
    }

    /**
     * 查询资金
     */
    @Test
    public void getAssert() {
        registerApi();
        RemoteAssertQueryRequest request = RemoteAssertQueryRequest.builder()
                .pairCode("BNB/USDT")
                .build();
        RemoteAssertQueryResponse response = remoteOrderService.getAssert(request);
        log.info("查询资金 request={}", JacksonUtil.toJSONStr(request));
        log.info("查询资金 response={}", JacksonUtil.toJSONStr(response));
    }


    /**
     * 查询订单
     */
    @Test
    public void getOrder() {
        registerApi();
        RemoteOrderQueryRequest request = RemoteOrderQueryRequest.builder()
                .pairCode("SXP/USDT")
                .localOrderId(null)
                .orderId("630315025")
                .build();
        RemoteOrderQueryResponse result = remoteOrderService.getOrder(request);
        log.info("查询订单 request={}", JacksonUtil.toJSONStr(request));
        log.info("查询订单 response={}", JacksonUtil.toJSONStr(result));
    }


}
