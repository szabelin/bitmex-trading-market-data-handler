package com.lunartrading.marketdata.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing the BitMEX WebSocket message action types.
 */
public enum ActionType {
    /**
     * Initial snapshot of the order book.
     * When received, you should clear your existing order book and use this as the baseline.
     */
    PARTIAL("partial"),

    /**
     * New orders being added to the order book.
     * Add these entries to your local order book.
     */
    INSERT("insert"),

    /**
     * Existing orders being modified (usually the size changes).
     * Find the matching order by ID and update its properties.
     */
    UPDATE("update"),

    /**
     * Orders being removed from the order book.
     * Remove these entries from your local order book by ID.
     */
    DELETE("delete"),

    /**
     * Unknown action type. Used for error handling.
     */
    UNKNOWN("unknown");

    private static final Map<String, ActionType> LOOKUP_MAP;

    static {
        Map<String, ActionType> map = new HashMap<>();
        for (ActionType type : ActionType.values()) {
            map.put(type.value, type);
        }
        LOOKUP_MAP = Collections.unmodifiableMap(map);
    }

    private final String value;

    ActionType(String value) {
        this.value = value;
    }

    /**
     * Get the ActionType enum for a given string value using a fast map lookup.
     *
     * @param value The string value to convert.
     * @return The corresponding ActionType, or UNKNOWN if not recognized.
     */
    public static ActionType fromString(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        return LOOKUP_MAP.getOrDefault(value.toLowerCase(), UNKNOWN);
    }
}