package com.openocean.arb.bot.network;

import com.openocean.arb.bot.config.NetworkProxyConfig;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * OKHttp客户端工厂
 *
 * @author lidong
 **/
@Component
public class OKHttpClientFactory {
    @Autowired
    private NetworkProxyConfig proxy;

    public OkHttpClient getClient() {
        return getBuilder().build();
    }

    public OkHttpClient getProxyedClient() {
        OkHttpClient.Builder builder = getBuilder();
        if (proxy.isEnabled()) {
            builder.proxy((new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxy.getHost(), proxy.getPort()))));
        }
        return builder.build();
    }

    public OkHttpClient getProxyedClientNonePing() {
        OkHttpClient.Builder builder = getBuilderNonePing();
        if (proxy.isEnabled()) {
            builder.proxy((new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxy.getHost(), proxy.getPort()))));
        }
        return builder.build();
    }

    private OkHttpClient.Builder getBuilder() {
        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .pingInterval(20, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS);
    }

    private OkHttpClient.Builder getBuilderNonePing() {
        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS);
    }
}
