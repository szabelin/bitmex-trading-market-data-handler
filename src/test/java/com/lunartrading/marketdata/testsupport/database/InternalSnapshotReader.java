package com.lunartrading.marketdata.testsupport.database;

import com.lunartrading.marketdata.orderbook.OrderBookSnapshot;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.processors.PublishProcessor;

import java.util.ArrayList;
import java.util.List;

public class InternalSnapshotReader implements SnapshotSource {

    private final List<OrderBookSnapshot> snapshots = new ArrayList<>();
    private final @NonNull Disposable subscribe;

    public InternalSnapshotReader(PublishProcessor<OrderBookSnapshot> snapshotProcessor) {
        subscribe = snapshotProcessor.subscribe(snapshots::add);
    }

    @Override
    public List<OrderBookSnapshot> getSnapshots(String symbol) {
        return snapshots.stream()
                .filter(s -> s.symbol().equals(symbol))
                .toList();
    }

    @Override
    public void close() {
        snapshots.clear();
        subscribe.dispose();
    }

}

