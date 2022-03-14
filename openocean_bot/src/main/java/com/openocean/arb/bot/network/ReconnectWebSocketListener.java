package com.openocean.arb.bot.network;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * @author lidong
 */
public abstract class ReconnectWebSocketListener extends WebSocketListener {

    public void onReconnected(WebSocket webSocket, Response response) {
    }

}
