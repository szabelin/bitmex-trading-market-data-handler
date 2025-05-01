package com.lunartrading.marketdata.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing the order side (Buy or Sell).
 */
public enum Side {
    /**
     * Buy side of the order book.
     */
    BUY("Buy"),

    /**
     * Sell side of the order book.
     */
    SELL("Sell"),

    /**
     * Unknown side. Used for error handling.
     */
    UNKNOWN("Unknown");

    private static final Map<String, Side> LOOKUP_MAP;

    static {
        Map<String, Side> map = new HashMap<>();
        for (Side side : Side.values()) {
            map.put(side.value.toLowerCase(), side);
        }
        LOOKUP_MAP = Collections.unmodifiableMap(map);
    }

    private final String value;

    Side(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get the Side enum for a given string value using a fast map lookup.
     *
     * @param value The string value to convert.
     * @return The corresponding Side, or UNKNOWN if not recognized.
     */
    public static Side fromString(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        return LOOKUP_MAP.getOrDefault(value.toLowerCase(), UNKNOWN);
    }
}