package com.lunartrading.marketdata.serialization;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.lunartrading.marketdata.domain.*;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FastBitMexMessageParser {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    public static BitMexMessage parse(String json) throws IOException {
        if (!json.contains("\"table\"")) {
            return BitMexMessage.INVALID;
        }

        try (JsonParser parser = JSON_FACTORY.createParser(json)) {
            return parseInternal(parser);
        }
    }

    private static BitMexMessage parseInternal(JsonParser parser) throws IOException {
        TableType table = null;
        ActionType action = null;
        List<OrderBookEntry> entries = new ArrayList<>();

        while (!parser.isClosed()) {
            JsonToken token = parser.nextToken();
            if (token == null) break;

            if (token == JsonToken.FIELD_NAME) {
                String fieldName = parser.currentName();
                parser.nextToken();

                switch (fieldName) {
                    case "table" -> table = TableType.fromString(parser.getText());
                    case "action" -> action = ActionType.fromString(parser.getText());
                    case "data" -> {
                        if (parser.currentToken() == JsonToken.START_ARRAY) {
                            while (parser.nextToken() != JsonToken.END_ARRAY) {
                                entries.add(parseEntry(parser));
                            }
                        }
                    }
                }
            }
        }

        return new BitMexMessage(table, action, List.copyOf(entries));
    }

    private static OrderBookEntry parseEntry(JsonParser parser) throws IOException {
        String symbol = null;
        long id = 0;
        Side side = null;
        long size = 0;
        double price = 0.0;
        Instant timestamp = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.currentName();
            parser.nextToken();

            switch (fieldName) {
                case "symbol" -> symbol = parser.getText();
                case "id" -> id = parser.getLongValue();
                case "side" -> side = Side.fromString(parser.getText());
                case "size" -> size = parser.getLongValue();
                case "price" -> price = parser.getDoubleValue();
                case "timestamp" -> timestamp = Instant.parse(parser.getText());
                default -> parser.skipChildren();
            }
        }

        return new OrderBookEntry(symbol, id, side, size, price, timestamp);
    }

}
