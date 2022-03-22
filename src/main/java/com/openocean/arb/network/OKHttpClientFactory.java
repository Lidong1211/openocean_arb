package com.openocean.arb.network;

import com.openocean.arb.client.holder.InputParamHolder;
import okhttp3.OkHttpClient;
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

    public OkHttpClient getClient() {
        return getBuilder().build();
    }

    public OkHttpClient getProxyedClient() {
        OkHttpClient.Builder builder = getBuilder();
        InputParamHolder.NetworkProxy proxy = InputParamHolder.networkProxy;
        if (proxy != null) {
            builder.proxy((new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxy.host, Integer.valueOf(proxy.port)))));
        }
        return builder.build();
    }

    public OkHttpClient getProxyedClientNonePing() {
        OkHttpClient.Builder builder = getBuilderNonePing();
        InputParamHolder.NetworkProxy proxy = InputParamHolder.networkProxy;
        if (proxy != null) {
            builder.proxy((new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxy.host, Integer.valueOf(proxy.port)))));
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
