package com.lunartrading.marketdata.websocket;

import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.annotation.PreDestroy;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class BitMexWebSocketClient {

    private static final Logger logger = LoggerFactory.getLogger(BitMexWebSocketClient.class);

    private final BitMexMessageProcessor rawMessageProcessor;
    private final String websocketUrl;
    private final WebSocket webSocket;
    private final OkHttpClient client;
    private final String symbols;

    @Autowired
    public BitMexWebSocketClient(
            @Value("XBTUSD,ETHUSD")
            String symbols,
            @Value("${bitmex.websocket.url:wss://www.bitmex.com/realtime}")
            String websocketUrl,
            BitMexMessageProcessor rawMessageProcessor) {
        this.symbols = symbols;
        this.rawMessageProcessor = rawMessageProcessor;
        this.websocketUrl = websocketUrl;

        this.client = new OkHttpClient.Builder()
                .pingInterval(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        logger.info("Connecting to BitMEX WebSocket API...");
        webSocket = connect();
    }

    private WebSocket connect() {
        Request request = new Request.Builder()
                .url(websocketUrl)
                .build();

        return client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                logger.info("WebSocket connection opened");

                String args = Arrays.stream(symbols.split(","))
                        .map(s -> "\"orderBookL2:" + s + "\"")
                        .collect(Collectors.joining(", "));

                String subscriptionMessage = String.format("{\"op\": \"subscribe\", \"args\": [%s]}", args);
                webSocket.send(subscriptionMessage);
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String rawJson) {
                rawMessageProcessor.process(rawJson);
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
                logger.error("WebSocket failure: {}", t.getMessage(), t);

                Schedulers.io().scheduleDirect(() -> {
                    logger.info("Attempting to reconnect in 5 seconds...");
                    try {
                        Thread.sleep(5000);
                        connect();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }

            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                logger.info("WebSocket closed: {} - {}", code, reason);
            }
        });

    }

    @PreDestroy
    public void onClose() {
        if (webSocket != null) {
            webSocket.close(1000, "application shutdown");
        }
    }

    public void close() {
        this.webSocket.close(1000, null);
    }
}