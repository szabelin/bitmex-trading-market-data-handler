package com.lunartrading.marketdata.orderbook;

import com.lunartrading.marketdata.domain.OrderBookEntry;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;

public record OrderBookSnapshot(
        String symbol,
        List<OrderBookEntry> bids,
        List<OrderBookEntry> asks,
        Instant snapshotTimestamp
) {
    public OrderBookSnapshot(
            String symbol,
            NavigableMap<Double, OrderBookEntry> bids,
            NavigableMap<Double, OrderBookEntry> asks,
            Instant mutableLatestTimestamp) {
        this(
                symbol,
                new ArrayList<>(bids.values()),
                new ArrayList<>(asks.values()),
                mutableLatestTimestamp
        );
    }

}