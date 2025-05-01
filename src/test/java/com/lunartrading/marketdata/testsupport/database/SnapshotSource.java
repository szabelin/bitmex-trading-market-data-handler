package com.lunartrading.marketdata.testsupport.database;

import com.lunartrading.marketdata.orderbook.OrderBookSnapshot;

import java.util.List;

public interface SnapshotSource {
    List<OrderBookSnapshot> getSnapshots(String symbol);

    void close();
}
