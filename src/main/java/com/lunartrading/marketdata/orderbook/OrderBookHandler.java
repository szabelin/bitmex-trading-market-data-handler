package com.lunartrading.marketdata.orderbook;

import com.lunartrading.marketdata.domain.ActionType;
import com.lunartrading.marketdata.domain.BitMexMessage;
import com.lunartrading.marketdata.domain.OrderBookEntry;
import com.lunartrading.marketdata.domain.Side;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;

/***
 * Single-threaded order book handler for BitMEX market data.
 * This class is thread safe
 */
public class OrderBookHandler {

    private static final Logger logger = LoggerFactory.getLogger(OrderBookHandler.class);

    private final String symbol;
    private final PublishProcessor<OrderBookSnapshot> snapshotPublisher;
    private final int topNLevels;

    private final Map<Long, OrderBookEntry> ordersById = new HashMap<>();

    private final TreeMap<Double, OrderBookEntry> bids =
            new TreeMap<>((a, b) -> Double.compare(b, a));

    private final TreeMap<Double, OrderBookEntry> asks =
            new TreeMap<>(Double::compare);

    //safe because always on same thread
    private Instant mutableLatestTimestamp = Instant.MIN;
    private double mutableLastBestBid;
    private double mutableLastBestAsk;
    private boolean mutableWillPublishSnapshots = false;

    private final Map<ActionType, Function<BitMexMessage, OrderBookSnapshot>> actions = Map.of(
            ActionType.PARTIAL, this::handlePartial,
            ActionType.INSERT, this::handleInsert,
            ActionType.UPDATE, this::handleUpdate,
            ActionType.DELETE, this::handleDelete,
            ActionType.UNKNOWN, this::handleUnknown
    );

    public OrderBookHandler(String symbol, PublishProcessor<OrderBookSnapshot> snapshotPublisher, int topNLevels) {
        this.symbol = symbol;
        this.snapshotPublisher = snapshotPublisher;
        this.topNLevels = topNLevels;
        resetLastPrices();
    }

    private void resetLastPrices() {
        mutableLastBestBid = Double.MIN_VALUE;
        mutableLastBestAsk = Double.MAX_VALUE;
        mutableWillPublishSnapshots = false;
    }

    public void handle(BitMexMessage message) {

        resetLastPrices();

        OrderBookSnapshot latestSnapshot = actions.getOrDefault(
                message.action(), this::handleUnknown
        ).apply(message);

        if (latestSnapshot != null) {
            snapshotPublisher.onNext(latestSnapshot);
        }
    }

    private OrderBookSnapshot handlePartial(BitMexMessage message) {
        bids.clear();
        asks.clear();
        ordersById.clear();

        updateLatestStats(message.data());

        for (OrderBookEntry entry : message.data()) {
            mutableWillPublishSnapshots = true;
            addOrder(entry);
        }

        return snapshot();
    }

    private OrderBookSnapshot handleInsert(BitMexMessage message) {

        updateLatestStats(message.data());

        for (OrderBookEntry entry : message.data()) {
            addOrder(entry);
        }

        return snapshot();
    }

    private OrderBookSnapshot handleUpdate(BitMexMessage message) {

        updateLatestStats(message.data());

        for (OrderBookEntry update : message.data()) {
            updateOrder(update);
        }

        return snapshot();
    }

    private OrderBookSnapshot handleDelete(BitMexMessage message) {

        updateLatestStats(message.data());

        for (OrderBookEntry entry : message.data()) {
            removeOrder(entry.id());
        }

        return snapshot();
    }

    private OrderBookSnapshot handleUnknown(BitMexMessage message) {
        logger.warn("Unknown message received: {}", message);
        return snapshot();
    }

    private void addOrder(OrderBookEntry entry) {
        ordersById.put(entry.id(), entry);
        if (entry.side() == Side.BUY) {
            bids.put(entry.price(), entry);
        } else if (entry.side() == Side.SELL) {
            asks.put(entry.price(), entry);
        }
    }

    private void updateOrder(OrderBookEntry update) {
        if (ordersById.get(update.id()) == null) {
            return;
        }

        ordersById.put(update.id(), update);
        if (update.side() == Side.BUY) {
            bids.put(update.price(), update);
        } else if (update.side() == Side.SELL) {
            asks.put(update.price(), update);
        }
    }

    private void removeOrder(long id) {
        OrderBookEntry existing = ordersById.remove(id);
        if (existing == null) {
            return;
        }

        if (existing.side() == Side.BUY) {
            bids.remove(existing.price());
        } else if (existing.side() == Side.SELL) {
            asks.remove(existing.price());
        }
    }

    private void updateLatestStats(List<OrderBookEntry> entries) {

        for (OrderBookEntry entry : entries) {
            if (entry.timestamp().isAfter(mutableLatestTimestamp)) {
                mutableLatestTimestamp = entry.timestamp();
            }

            if (entry.side().equals(Side.BUY)) {
                if (mutableLastBestBid < entry.price()) {
                    mutableLastBestBid = entry.price();
                }
            } else {
                if (mutableLastBestAsk > entry.price()) {
                    mutableLastBestAsk = entry.price();
                }
            }
        }

        publishingUpdateMightBeNeeded();
    }

    private void publishingUpdateMightBeNeeded() {

        mutableWillPublishSnapshots = false;

        if (this.topNLevels == 0) {
            this.mutableWillPublishSnapshots = true;
            return;
        }

        NavigableMap<Double, OrderBookEntry> bidsTop = bids.headMap(mutableLastBestBid, true);
        NavigableMap<Double, OrderBookEntry> asksTop = asks.headMap(mutableLastBestAsk, true);
        int bidPosition = bidsTop.size();
        int askPosition = asksTop.size();

        if (bidPosition <= this.topNLevels || askPosition <= this.topNLevels) {
            this.mutableWillPublishSnapshots = true;
        }

    }

    private OrderBookSnapshot snapshot() {
        if (this.topNLevels == 0) {
            return new OrderBookSnapshot(symbol, bids, asks, mutableLatestTimestamp);
        }

        if (this.mutableWillPublishSnapshots) {
            NavigableMap<Double, OrderBookEntry> topNBids = topNFrom(bids, topNLevels);
            NavigableMap<Double, OrderBookEntry> topNAsks = topNFrom(asks, topNLevels);

            return new OrderBookSnapshot(symbol, topNBids, topNAsks, mutableLatestTimestamp);
        }

        return null;
    }

    private static NavigableMap<Double, OrderBookEntry> topNFrom(TreeMap<Double, OrderBookEntry> source, int N) {
        NavigableMap<Double, OrderBookEntry> result = new TreeMap<>(source.comparator());
        int count = 0;
        for (Map.Entry<Double, OrderBookEntry> entry : source.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
            if (++count >= N) break;
        }
        return result;
    }

}
