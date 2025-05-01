package com.lunartrading.marketdata.testsupport.websocket;

import com.lunartrading.marketdata.domain.OrderBookEntry;
import com.lunartrading.marketdata.testsupport.SideBuilder;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.lunartrading.DomainObjectFactory.*;
import static com.lunartrading.marketdata.testsupport.SideBuilder.collectEntries;

public class ExternalExchangeSimulator implements ExchangeSimulator {

    private final WebsocketPublisherForTests websocket;

    public ExternalExchangeSimulator(WebsocketPublisherForTests websocket) {
        this.websocket = websocket;
    }

    @Override
    public void publishesPartial(Consumer<SideBuilder> bidBuilder,
                                 Consumer<SideBuilder> askBuilder) {
        publish(bidBuilder, askBuilder, orderBookEntries -> partialMessage(orderBookEntries).asJson());
    }

    @Override
    public void publishesDelete(Consumer<SideBuilder> bidBuilder,
                                Consumer<SideBuilder> askBuilder) {
        publish(bidBuilder, askBuilder, orderBookEntries -> deleteMessage(orderBookEntries).asJson());
    }

    @Override
    public void publishesUpdate(Consumer<SideBuilder> bidBuilder,
                                Consumer<SideBuilder> askBuilder) {
        publish(bidBuilder, askBuilder, orderBookEntries -> updateMessage(orderBookEntries).asJson());
    }

    @Override
    public void publishesInsert(Consumer<SideBuilder> bidBuilder,
                                Consumer<SideBuilder> askBuilder) {
        publish(bidBuilder, askBuilder, orderBookEntries -> insertMessage(orderBookEntries).asJson());
    }

    private void publish(Consumer<SideBuilder> bidBuilder,
                         Consumer<SideBuilder> askBuilder,
                         Function<List<OrderBookEntry>, String> messageCreator) {

        List<OrderBookEntry> orderBookEntries = collectEntries(bidBuilder, askBuilder);
        String rawJson = messageCreator.apply(orderBookEntries);
        this.websocket.publishToAllClients(rawJson);
    }
}
