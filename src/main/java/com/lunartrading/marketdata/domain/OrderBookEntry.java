package com.lunartrading.marketdata.domain;

import java.time.Instant;

public record OrderBookEntry(
        String symbol,
        long id,
        Side side,
        long size,
        double price,
        Instant timestamp
) {
}
