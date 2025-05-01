package com.lunartrading.marketdata.service;

import com.lunartrading.marketdata.config.GracefulShutdown;
import com.lunartrading.marketdata.domain.BitMexMessage;
import com.lunartrading.marketdata.domain.OrderBookEntry;
import com.lunartrading.marketdata.domain.TableType;
import com.lunartrading.marketdata.orderbook.OrderBookHandler;
import com.lunartrading.marketdata.orderbook.OrderBookSnapshot;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.lunartrading.marketdata.config.ApplicationShutdown.closeRxSubscription;

/***
 * Subscribes to message updates and forwards them to correct handlers
 */
@Service
public class OrderBookService implements GracefulShutdown {

    private static final Logger logger = LoggerFactory.getLogger(OrderBookService.class);

    private final PublishProcessor<BitMexMessage> messageSubject;
    private final Disposable disposable;

    private final Map<String, OrderBookHandler> booksBySymbol = new HashMap<>();
    private final PublishProcessor<OrderBookSnapshot> snapshotProcessor;
    private final int topNLevels;

    public OrderBookService(PublishProcessor<BitMexMessage> messageSubject,
                            PublishProcessor<OrderBookSnapshot> snapshotProcessor,
                            @Value("${orderbook.snapshot.levels:0}") int topNLevels) {
        this.messageSubject = messageSubject;
        this.snapshotProcessor = snapshotProcessor;
        this.topNLevels = topNLevels;
        this.disposable = listen();
    }

    private Disposable listen() {
        return messageSubject
                .onBackpressureBuffer(10_000, () -> logger.error("Backpressure buffer overflow"))
                .filter(bitMexMessage -> bitMexMessage.table().equals(TableType.ORDER_BOOK_L2))
                .observeOn(Schedulers.single())
                .doOnError(throwable -> {
                    logger.error("error: {}", throwable.getMessage(), throwable);
                })
                .subscribe(message -> {

                    if (message.data().isEmpty()) {
                        return;
                    }

                    OrderBookEntry bookEntry = message.data().getFirst();
                    String symbol = bookEntry.symbol();

                    booksBySymbol.putIfAbsent(symbol, new OrderBookHandler(symbol, snapshotProcessor, topNLevels));
                    booksBySymbol.get(symbol).handle(message);

                });
    }

    @Override
    public CompletableFuture<Void> shutdown() {
        return closeRxSubscription(disposable);
    }
}
