package com.lunartrading.marketdata.testsupport.websocket;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class WebsocketPublisherForTests extends WebSocketServer {

    private final Set<WebSocket> connections = new CopyOnWriteArraySet<>();

    public WebsocketPublisherForTests() {
        super(new InetSocketAddress(8080));
        start();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {

    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {

    }

    @Override
    public void onStart() {
    }

    public void publishToAllClients(String json) {
        for (WebSocket conn : connections) {
            conn.send(json);
        }
    }

    public void close() throws InterruptedException {
        stop();
    }
}
