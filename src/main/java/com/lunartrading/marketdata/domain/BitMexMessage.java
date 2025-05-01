package com.lunartrading.marketdata.domain;

import java.util.List;

public record BitMexMessage(
        TableType table,
        ActionType action,
        List<OrderBookEntry> data
) {

    public static BitMexMessage INVALID = new BitMexMessage(null, null, List.of());

}
