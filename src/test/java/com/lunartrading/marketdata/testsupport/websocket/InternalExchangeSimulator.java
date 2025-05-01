package com.lunartrading.marketdata.testsupport.websocket;

import com.lunartrading.marketdata.domain.BitMexMessage;
import com.lunartrading.marketdata.domain.OrderBookEntry;
import com.lunartrading.marketdata.testsupport.SideBuilder;
import io.reactivex.rxjava3.processors.PublishProcessor;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.lunartrading.DomainObjectFactory.*;
import static com.lunartrading.marketdata.testsupport.SideBuilder.collectEntries;

public class InternalExchangeSimulator implements ExchangeSimulator {

    private final PublishProcessor<BitMexMessage> messageProcessor;

    public InternalExchangeSimulator(PublishProcessor<BitMexMessage> messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    @Override
    public void publishesPartial(Consumer<SideBuilder> bidBuilder,
                                 Consumer<SideBuilder> askBuilder) {
        publish(bidBuilder, askBuilder, orderBookEntries -> partialMessage(orderBookEntries).asMessage());
    }

    @Override
    public void publishesDelete(Consumer<SideBuilder> bidBuilder,
                                Consumer<SideBuilder> askBuilder) {
        publish(bidBuilder, askBuilder, orderBookEntries -> deleteMessage(orderBookEntries).asMessage());
    }

    @Override
    public void publishesUpdate(Consumer<SideBuilder> bidBuilder,
                                Consumer<SideBuilder> askBuilder) {
        publish(bidBuilder, askBuilder, orderBookEntries -> updateMessage(orderBookEntries).asMessage());
    }

    @Override
    public void publishesInsert(Consumer<SideBuilder> bidBuilder,
                                Consumer<SideBuilder> askBuilder) {
        publish(bidBuilder, askBuilder, orderBookEntries -> insertMessage(orderBookEntries).asMessage());
    }

    private void publish(Consumer<SideBuilder> bidBuilder,
                         Consumer<SideBuilder> askBuilder,
                         Function<List<OrderBookEntry>, BitMexMessage> messageCreator) {

        List<OrderBookEntry> orderBookEntries = collectEntries(bidBuilder, askBuilder);
        BitMexMessage message = messageCreator.apply(orderBookEntries);
        messageProcessor.onNext(message);
    }
}
