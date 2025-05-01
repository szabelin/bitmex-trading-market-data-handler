package com.lunartrading.marketdata.testsupport;

import com.lunartrading.DomainObjectFactory;
import com.lunartrading.marketdata.domain.OrderBookEntry;
import com.lunartrading.marketdata.domain.Side;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import static com.lunartrading.DomainObjectFactory.DEFAULT_SYMBOL;
import static com.lunartrading.DomainObjectFactory.DEFAULT_TIMESTAMP;

public class SideBuilder {

    private final List<OrderBookEntry> entries = new ArrayList<>();
    private final Side side;
    private String symbol = DEFAULT_SYMBOL;

    public SideBuilder(Side side) {
        this.side = side;
    }

    public static List<OrderBookEntry> collectEntries(
            Consumer<SideBuilder> bidBuilder,
            Consumer<SideBuilder> askBuilder) {
        SideBuilder bids = new SideBuilder(Side.BUY);
        SideBuilder asks = new SideBuilder(Side.SELL);

        bidBuilder.accept(bids);
        askBuilder.accept(asks);

        List<OrderBookEntry> entries = new ArrayList<>(bids.buildEntries(Side.BUY));
        entries.addAll(asks.buildEntries(Side.SELL));

        return entries;
    }

    public static List<OrderBookEntry> collectEntries(
            Consumer<SideBuilder> builder, Side side) {
        SideBuilder sideBuilder = new SideBuilder(side);
        builder.accept(sideBuilder);
        return sideBuilder.buildEntries(side);
    }

    public SideBuilder level(double price, long size) {
        return level(price, size, DEFAULT_TIMESTAMP);
    }

    public SideBuilder level(double price, long size, Instant timestamp) {
        entries.add(new OrderBookEntry(
                symbol,
                DomainObjectFactory.getIdForPrice(price, side),
                side,
                size,
                price,
                timestamp
        ));
        return this;
    }

    public SideBuilder level(double price) {
        return level(price, 0L, DEFAULT_TIMESTAMP);
    }

    public List<OrderBookEntry> buildEntries(Side side) {
        return entries.stream()
                .map(entry -> new OrderBookEntry(
                        entry.symbol(),
                        entry.id(),
                        side,
                        entry.size(),
                        entry.price(),
                        entry.timestamp()
                ))
                .sorted(side.equals(Side.BUY) ? Comparator.comparingDouble(OrderBookEntry::price).reversed() :
                        Comparator.comparingDouble(OrderBookEntry::price))
                .toList();
    }

    public SideBuilder withSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }
}
