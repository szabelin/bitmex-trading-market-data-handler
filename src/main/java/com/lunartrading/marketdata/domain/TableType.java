package com.lunartrading.marketdata.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing the BitMEX WebSocket message table types.
 */
public enum TableType {
    /**
     * L2 Order Book updates.
     */
    ORDER_BOOK_L2("orderBookL2"),

    /**
     * Trade data.
     */
    TRADE("trade"),

    /**
     * Instrument data.
     */
    INSTRUMENT("instrument"),

    /**
     * Unknown table type. Used for error handling.
     */
    UNKNOWN("unknown");

    private static final Map<String, TableType> LOOKUP_MAP;

    static {
        Map<String, TableType> map = new HashMap<>();
        for (TableType type : TableType.values()) {
            map.put(type.value.toLowerCase(), type);
        }
        LOOKUP_MAP = Collections.unmodifiableMap(map);
    }

    private final String value;

    TableType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get the TableType enum for a given string value using a fast map lookup.
     *
     * @param value The string value to convert.
     * @return The corresponding TableType, or UNKNOWN if not recognized.
     */
    public static TableType fromString(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        return LOOKUP_MAP.getOrDefault(value.toLowerCase(), UNKNOWN);
    }
}