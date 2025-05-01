package com.lunartrading.marketdata.testsupport;

import com.lunartrading.marketdata.domain.BitMexMessage;
import com.lunartrading.marketdata.testsupport.websocket.ExchangeSimulator;
import com.lunartrading.marketdata.testsupport.websocket.ExternalExchangeSimulator;
import com.lunartrading.marketdata.testsupport.websocket.InternalExchangeSimulator;
import com.lunartrading.marketdata.testsupport.websocket.WebsocketPublisherForTests;
import com.lunartrading.marketdata.websocket.BitMexMessageProcessor;
import com.lunartrading.marketdata.websocket.BitMexWebSocketClient;
import io.reactivex.rxjava3.processors.PublishProcessor;

public class Given {
    private final ExchangeSimulator simulator;
    private final BitMexWebSocketClient webSocketClient;
    private final WebsocketPublisherForTests websocketPublisher;

    public Given(PublishProcessor<BitMexMessage> messagePublisher, TestingLevel testingLevel) {
        if (testingLevel.equals(TestingLevel.INTEGRATION)) {
            websocketPublisher = new WebsocketPublisherForTests();
            this.simulator = new ExternalExchangeSimulator(websocketPublisher);
            webSocketClient = new BitMexWebSocketClient(
                    "XBTUSD,ETHUSD",
                    "ws://localhost:8080",
            new BitMexMessageProcessor(messagePublisher));
        } else {
            this.simulator = new InternalExchangeSimulator(messagePublisher);
            this.webSocketClient = null;
            this.websocketPublisher = null;
        }
    }

    public ExchangeSimulator exchange() {
        return simulator;
    }

    public void clear() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
        if (websocketPublisher != null) {
            try {
                websocketPublisher.close();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
