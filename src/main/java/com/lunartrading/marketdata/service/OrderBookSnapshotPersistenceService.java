package com.lunartrading.marketdata.service;

import com.lunartrading.marketdata.config.GracefulShutdown;
import com.lunartrading.marketdata.domain.OrderBookEntry;
import com.lunartrading.marketdata.orderbook.OrderBookSnapshot;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.lunartrading.marketdata.config.ApplicationShutdown.closeRxSubscription;

/***
 * Persists snapshots to DB
 */
@Service
public class OrderBookSnapshotPersistenceService implements GracefulShutdown {

    private static final Logger logger = LoggerFactory.getLogger(OrderBookSnapshotPersistenceService.class);

    private final JdbcTemplate jdbcTemplate;
    private final Disposable subscribe;

    public OrderBookSnapshotPersistenceService(
            PublishProcessor<OrderBookSnapshot> snapshotProcessor,
            JdbcTemplate jdbcTemplate) {
        subscribe = snapshotProcessor
                .onBackpressureBuffer(10_000, () ->
                        logger.error("Backpressure buffer overflow"))
                .observeOn(Schedulers.io())
                .doOnError(throwable ->
                        logger.error("Error during persistence", throwable))
                .subscribe(this::saveSnapshot,
                        throwable ->
                                logger.error("Error during snapshot persistence", throwable));

        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveSnapshot(OrderBookSnapshot snapshot) {
        logger.info("Persisting snapshot for symbol={}", snapshot.symbol());

        long snapshotId;
        try {
            snapshotId = saveSnapshotRecord(snapshot);
        } catch (Exception e) {
            logger.error("Error saving snapshot", e);
            return;
        }

        List<OrderBookEntry> entries = null;
        try {
            entries = new ArrayList<>();
            entries.addAll(snapshot.bids());
            entries.addAll(snapshot.asks());

            for (OrderBookEntry entry : entries) {
                jdbcTemplate.update("""
                                    INSERT INTO order_book_levels
                                        (snapshot_id, side, price, size, level_id)
                                    VALUES (?, ?, ?, ?, ?)
                                """,
                        snapshotId,
                        entry.side().name(),
                        entry.price(),
                        entry.size(),
                        entry.id()
                );
            }
        } catch (DataAccessException e) {
            logger.error("Error saving book level snapshot", e);
            return;
        }

        logger.info("Saved snapshot {} with {} levels", snapshotId, entries.size());
    }

    private long saveSnapshotRecord(OrderBookSnapshot snapshot) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO order_book_snapshots (symbol, timestamp) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, snapshot.symbol());
            ps.setTimestamp(2, Timestamp.from(snapshot.snapshotTimestamp()));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to retrieve snapshot_id");
        }
        return key.longValue();
    }

    @Override
    public CompletableFuture<Void> shutdown() {
        return closeRxSubscription(subscribe);
    }
}
