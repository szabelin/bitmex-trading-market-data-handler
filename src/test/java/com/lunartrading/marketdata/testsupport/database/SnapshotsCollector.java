package com.lunartrading.marketdata.testsupport.database;

import com.lunartrading.marketdata.domain.OrderBookEntry;
import com.lunartrading.marketdata.domain.Side;
import com.lunartrading.marketdata.orderbook.OrderBookSnapshot;
import com.lunartrading.marketdata.testsupport.SideBuilder;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.lunartrading.marketdata.testsupport.SideBuilder.collectEntries;

public class SnapshotsCollector {

    private final Supplier<List<OrderBookSnapshot>> snapshotsSupplier;
    private final String symbol;

    public SnapshotsCollector(Supplier<List<OrderBookSnapshot>> snapshotsSupplier, String symbol) {
        this.snapshotsSupplier = snapshotsSupplier;
        this.symbol = symbol;
    }

    public void hasBook(
            Consumer<SideBuilder> bidBuilder,
            Consumer<SideBuilder> askBuilder) {

        List<OrderBookEntry> bids = collectEntries(bidBuilder, Side.BUY);
        List<OrderBookEntry> asks = collectEntries(askBuilder, Side.SELL);

        Awaitility.await().atMost(Duration.ofSeconds(5)).until(() -> {
            List<OrderBookSnapshot> snapshots = snapshotsSupplier.get();
            for (int i = snapshots.size() - 1; i >= 0; i--) {
                OrderBookSnapshot snapshot = snapshots.get(i);
                if (snapshot.symbol().equals(symbol)) {
                    return snapshot.bids().equals(bids) &&
                           snapshot.asks().equals(asks);
                }
            }
            return false;
        });
    }

    public void hasLatestTimestamp(Instant timestamp) {
        Awaitility.await().atMost(Duration.ofSeconds(5)).until(() -> {
            List<OrderBookSnapshot> snapshots = snapshotsSupplier.get();
            if (snapshots.isEmpty()) return false;
            OrderBookSnapshot latest = snapshots.getLast();
            return latest.snapshotTimestamp().equals(timestamp);
        });
    }

    public void hasSnapshotCount(int expectedCount) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(1000);
        Assertions.assertThat(this.snapshotsSupplier.get()).hasSize(expectedCount);
    }
}
