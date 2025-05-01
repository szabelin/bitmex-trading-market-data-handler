package com.lunartrading;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lunartrading.marketdata.domain.*;
import com.lunartrading.marketdata.errors.DefectError;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating BitMEX message specifications for tests.
 */
public interface DomainObjectFactory {

    String DEFAULT_SYMBOL = "XBTUSD";
    String ANOTHER_SYMBOL = "ETHUSD";

    Instant DEFAULT_TIMESTAMP = Instant.parse("2024-01-03T00:00:00.000Z");
    Instant TIMESTAMP_1 = Instant.parse("2025-01-01T00:00:00.000Z");
    Instant TIMESTAMP_2 = Instant.parse("2025-01-02T00:00:00.000Z");

    // BID price constants (descending)
    double PRICE200_0 = 200.0;
    double PRICE199_5 = 199.5;
    double PRICE199_0 = 199.0;
    double PRICE198_5 = 198.5;

    // ASK price constants (ascending)
    double PRICE200_5 = 200.5;
    double PRICE201_0 = 201.0;
    double PRICE201_5 = 201.5;
    double PRICE202_0 = 202.0;

    // level ID mappings so that testers do need to supply them
    Map<Double, Long> BID_PRICE_TO_ID = Map.of(
            PRICE200_0, 1000L,
            PRICE199_5, 1001L,
            PRICE199_0, 1002L,
            PRICE198_5, 1003L
    );

    Map<Double, Long> ASK_PRICE_TO_ID = Map.of(
            PRICE200_5, 2000L,
            PRICE201_0, 2001L,
            PRICE201_5, 2002L,
            PRICE202_0, 2003L
    );

    static long getIdForPrice(double price, Side side) {
        Map<Double, Long> map = side.equals(Side.BUY) ? BID_PRICE_TO_ID : ASK_PRICE_TO_ID;
        if (!map.containsKey(price)) {
            throw new DefectError("Price " + price + " does not map to any level ID.");
        }
        return map.get(price);
    }

    static PartialMessage partialMessage(List<OrderBookEntry> orderBookEntries) {
        return new PartialMessage(orderBookEntries);
    }

    static UpdateMessage updateMessage(List<OrderBookEntry> orderBookEntries) {
        return new UpdateMessage(orderBookEntries);
    }

    static InsertMessage insertMessage(List<OrderBookEntry> orderBookEntries) {
        return new InsertMessage(orderBookEntries);
    }

    static DeleteMessage deleteMessage(List<OrderBookEntry> orderBookEntries) {
        return new DeleteMessage(orderBookEntries);
    }


    final class InsertMessage extends Message {

        public InsertMessage(List<OrderBookEntry> orderBookEntries) {
            super(orderBookEntries);
        }

        public BitMexMessage asMessage() {
            return asMessage(ActionType.INSERT);
        }

        public String asJson() {
            return asJson(ActionType.INSERT);
        }
    }

    final class DeleteMessage extends Message {

        public DeleteMessage(List<OrderBookEntry> orderBookEntries) {
            super(orderBookEntries);
        }

        public BitMexMessage asMessage() {
            return asMessage(ActionType.DELETE);
        }

        public String asJson() {
            return asJson(ActionType.DELETE);
        }
    }

    final class UpdateMessage extends Message {

        public UpdateMessage(List<OrderBookEntry> orderBookEntries) {
            super(orderBookEntries);
        }

        public BitMexMessage asMessage() {
            return asMessage(ActionType.UPDATE);
        }

        public String asJson() {
            return asJson(ActionType.UPDATE);
        }
    }

    final class PartialMessage extends Message {

        public PartialMessage(List<OrderBookEntry> orderBookEntries) {
            super(orderBookEntries);
        }

        public BitMexMessage asMessage() {
            return asMessage(ActionType.PARTIAL);
        }

        public String asJson() {
            return asJson(ActionType.PARTIAL);
        }
    }


    class Message {

        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
        private final List<OrderBookEntry> orderBookEntries;

        public Message(List<OrderBookEntry> orderBookEntries) {
            this.orderBookEntries = orderBookEntries;
        }

        protected BitMexMessage asMessage(ActionType actionType) {
            return new BitMexMessage(
                    TableType.ORDER_BOOK_L2,
                    actionType,
                    orderBookEntries
            );
        }

        protected String asJson(ActionType actionType) {
            ObjectNode root = OBJECT_MAPPER.createObjectNode();
            root.put("table", "orderBookL2");
            root.put("action", actionType.name().toLowerCase());

            // Add type-specific fields
            if (actionType == ActionType.PARTIAL) {
                root.putArray("keys").add("symbol").add("id").add("side");

                ObjectNode types = root.putObject("types");
                types.put("symbol", "symbol");
                types.put("id", "long");
                types.put("side", "symbol");
                types.put("size", "long");
                types.put("price", "float");
                types.put("timestamp", "timestamp");
                types.put("transactTime", "timestamp");

                if (!orderBookEntries.isEmpty()) {
                    root.putObject("filter").put("symbol", orderBookEntries.get(0).symbol());
                }
            }

            ArrayNode dataArray = root.putArray("data");
            for (OrderBookEntry entry : orderBookEntries) {
                ObjectNode entryNode = OBJECT_MAPPER.createObjectNode();
                entryNode.put("symbol", entry.symbol());
                entryNode.put("id", entry.id());
                entryNode.put("side", entry.side() == Side.BUY ? "Buy" : "Sell");

                // size only for insert, update, partial
                if (actionType != ActionType.DELETE) {
                    entryNode.put("size", entry.size());
                }

                entryNode.put("price", entry.price());
                entryNode.put("timestamp", entry.timestamp().toString());
                entryNode.put("transactTime", entry.timestamp().toString()); // You can replace with different value if needed

                dataArray.add(entryNode);
            }

            return root.toPrettyString();
        }
    }

}
