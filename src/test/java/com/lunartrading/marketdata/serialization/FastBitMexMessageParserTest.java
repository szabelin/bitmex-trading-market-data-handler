package com.lunartrading.marketdata.serialization;

import com.lunartrading.marketdata.domain.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FastBitMexMessageParserTest {

    @Test
    void shouldParsePartialOrderBookCorrectly() throws Exception {
        String json = """
                {
                  "table": "orderBookL2",
                  "action": "partial",
                  "keys": ["symbol", "id", "side"],
                  "types": {
                    "symbol": "symbol",
                    "id": "long",
                    "side": "symbol",
                    "size": "long",
                    "price": "float",
                    "timestamp": "timestamp",
                    "transactTime": "timestamp"
                  },
                  "filter": {
                    "symbol": "XBTUSD"
                  },
                  "data": [
                    {
                      "symbol": "XBTUSD",
                      "id": 17180110699,
                      "side": "Sell",
                      "size": 900,
                      "price": 3201030.0,
                      "timestamp": "2025-04-25T21:18:20.859Z",
                      "transactTime": "2025-04-25T02:00:00.002Z"
                    },
                    {
                      "symbol": "XBTUSD",
                      "id": 1000330,
                      "side": "Sell",
                      "size": 1000000,
                      "price": 1000000.0,
                      "timestamp": "2025-04-25T21:18:20.859Z",
                      "transactTime": "2025-04-25T02:00:00.002Z"
                    },
                    {
                      "symbol": "XBTUSD",
                      "id": 25039222234,
                      "side": "Sell",
                      "size": 33800,
                      "price": 676700.0,
                      "timestamp": "2025-04-25T21:18:20.859Z",
                      "transactTime": "2025-04-25T02:00:00.002Z"
                    }
                  ]
                }
                """;

        BitMexMessage message = FastBitMexMessageParser.parse(json);

        // Basic message assertions
        assertNotNull(message);
        assertEquals(TableType.ORDER_BOOK_L2, message.table());
        assertEquals(ActionType.PARTIAL, message.action());

        List<OrderBookEntry> entries = message.data();
        assertNotNull(entries);
        assertEquals(3, entries.size());

        // Check first entry
        OrderBookEntry first = entries.getFirst();
        assertEquals("XBTUSD", first.symbol());
        assertEquals(17180110699L, first.id());
        assertEquals(Side.SELL, first.side());
        assertEquals(900L, first.size());
        assertEquals(3201030.0, first.price());
        assertEquals(Instant.parse("2025-04-25T21:18:20.859Z"), first.timestamp());

        // Check second entry
        OrderBookEntry second = entries.get(1);
        assertEquals("XBTUSD", second.symbol());
        assertEquals(1000330L, second.id());
        assertEquals(Side.SELL, second.side());
        assertEquals(1000000L, second.size());
        assertEquals(1000000.0, second.price());
        assertEquals(Instant.parse("2025-04-25T21:18:20.859Z"), second.timestamp());

        // Check third entry
        OrderBookEntry third = entries.get(2);
        assertEquals("XBTUSD", third.symbol());
        assertEquals(25039222234L, third.id());
        assertEquals(Side.SELL, third.side());
        assertEquals(33800L, third.size());
        assertEquals(676700.0, third.price());
        assertEquals(Instant.parse("2025-04-25T21:18:20.859Z"), third.timestamp());
    }
}
