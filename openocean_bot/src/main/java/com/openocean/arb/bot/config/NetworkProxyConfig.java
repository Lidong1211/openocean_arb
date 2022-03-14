package com.openocean.arb.bot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 网络代理配置
 *
 * @author lidong
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "network.proxy")
public class NetworkProxyConfig {
    private boolean enabled;
    private String host;
    private Integer port;
}
