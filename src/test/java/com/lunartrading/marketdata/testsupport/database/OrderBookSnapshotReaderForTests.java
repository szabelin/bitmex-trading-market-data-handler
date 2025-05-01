package com.lunartrading.marketdata.testsupport.database;

import com.lunartrading.marketdata.domain.OrderBookEntry;
import com.lunartrading.marketdata.domain.Side;
import com.lunartrading.marketdata.orderbook.OrderBookSnapshot;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.*;

public class OrderBookSnapshotReaderForTests {

    private final JdbcTemplate jdbcTemplate;

    public OrderBookSnapshotReaderForTests(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<OrderBookSnapshot> readAllSnapshots() {
        Map<Long, SnapshotBuilder> builders = new LinkedHashMap<>();

        jdbcTemplate.query("""
                    SELECT
                        s.snapshot_id,
                        s.symbol,
                        s.timestamp,
                        l.side,
                        l.price,
                        l.size,
                        l.level_id
                    FROM order_book_snapshots s
                    JOIN order_book_levels l ON s.snapshot_id = l.snapshot_id
                    ORDER BY s.snapshot_id, l.side, l.price
                """, rs -> {
            long snapshotId = rs.getLong("snapshot_id");
            String symbol = rs.getString("symbol");
            Instant timestamp = rs.getTimestamp("timestamp").toInstant();

            SnapshotBuilder builder = builders.computeIfAbsent(snapshotId,
                    id -> new SnapshotBuilder(snapshotId, symbol, timestamp));

            OrderBookEntry entry = new OrderBookEntry(
                    symbol,
                    rs.getLong("level_id"),
                    Side.valueOf(rs.getString("side").toUpperCase()),
                    rs.getLong("size"),
                    rs.getDouble("price"),
                    timestamp
            );

            builder.add(entry);
        });

        return builders.values().stream().map(SnapshotBuilder::build).toList();
    }

    private static class SnapshotBuilder {
        final long snapshotId;
        final String symbol;
        final Instant timestamp;
        final List<OrderBookEntry> bids = new ArrayList<>();
        final List<OrderBookEntry> asks = new ArrayList<>();

        SnapshotBuilder(long snapshotId, String symbol, Instant timestamp) {
            this.snapshotId = snapshotId;
            this.symbol = symbol;
            this.timestamp = timestamp;
        }

        void add(OrderBookEntry entry) {
            if (entry.side() == Side.BUY) {
                bids.add(entry);
            } else {
                asks.add(entry);
            }
        }

        OrderBookSnapshot build() {
            bids.sort(Comparator.comparing(OrderBookEntry::price).reversed());
            asks.sort(Comparator.comparing(OrderBookEntry::price));

            return new OrderBookSnapshot(symbol, bids, asks, timestamp);
        }
    }
}
