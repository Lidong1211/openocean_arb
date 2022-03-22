package com.openocean.arb.config.remote.openocean;

import com.openocean.arb.config.remote.BaseRemoteConfig;
import feign.Feign;
import feign.Logger;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.querymap.BeanQueryMapEncoder;
import feign.slf4j.Slf4jLogger;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenOcean配置
 *
 * @author lidong
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "network.remote.openocean")
public class OpenOceanRemoteConfig extends BaseRemoteConfig {

    /**
     * API
     */
    private String apiV2Url;

    @Bean
    public OpenOceanV2Api createOpenOceanV2Api() {
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .client(new OkHttpClient())
                .retryer(new Retryer.Default(100L, 1000L, 1))
                .logger(new Slf4jLogger())
                .logLevel(Logger.Level.FULL)
                .queryMapEncoder(new BeanQueryMapEncoder())
                .target(OpenOceanV2Api.class, apiV2Url);
    }

}
