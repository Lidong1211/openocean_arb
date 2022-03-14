package com.openocean.arb.bot.model.remote;

import lombok.Builder;
import lombok.Data;

/**
 * 远程注册API请求
 *
 * @author lidong
 */
@Data
@Builder
public class RemoteRegisterApiRequest {
    // API KEY
    private String apiKey;
    // 秘钥
    private String secret;
    // 密码
    private String passPhrase;
    // 现货账号
    private String spotAccountId;

}
