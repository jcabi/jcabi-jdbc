/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.jdbc;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default mappings for types.
 * @since 0.17.6
 */
final class DefaultMappings implements Outcome.Mappings {

    /**
     * Per-type extraction methods.
     */
    private final Map<Class<?>, Outcome.Mapping<?>> map;

    /**
     * Ctor.
     */
    DefaultMappings() {
        this(1);
    }

    /**
     * Ctor.
     * @param column Column position
     */
    // @checkstyle ConstructorsCodeFreeCheck (3 lines)
    DefaultMappings(final int column) {
        this(DefaultMappings.defaults(column));
    }

    /**
     * Primary ctor.
     * @param mpp The mappings map
     */
    private DefaultMappings(final Map<Class<?>, Outcome.Mapping<?>> mpp) {
        this.map = mpp;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> Outcome.Mapping<X> forType(final Class<? extends X> type) {
        if (!this.map.containsKey(type)) {
            throw new IllegalArgumentException(
                String.format("Type %s is not supported", type.getName())
            );
        }
        return (Outcome.Mapping<X>) this.map.get(type);
    }

    /**
     * Build the default mappings for a given column.
     * @param column Column position
     * @return The default mappings map
     */
    private static Map<Class<?>, Outcome.Mapping<?>> defaults(final int column) {
        return Stream.<Map.Entry<Class<?>, Outcome.Mapping<?>>>of(
            new AbstractMap.SimpleImmutableEntry<>(
                String.class, rs -> rs.getString(column)
            ),
            new AbstractMap.SimpleImmutableEntry<>(
                Long.class, rs -> rs.getLong(column)
            ),
            new AbstractMap.SimpleImmutableEntry<>(
                Boolean.class, rs -> rs.getBoolean(column)
            ),
            new AbstractMap.SimpleImmutableEntry<>(
                Byte.class, rs -> rs.getByte(column)
            ),
            new AbstractMap.SimpleImmutableEntry<>(
                Date.class, rs -> rs.getDate(column)
            ),
            new AbstractMap.SimpleImmutableEntry<>(
                Utc.class, rs -> new Utc(Utc.getTimestamp(rs, column))
            ),
            new AbstractMap.SimpleImmutableEntry<>(
                byte[].class, rs -> rs.getBytes(column)
            ),
            new AbstractMap.SimpleImmutableEntry<>(
                BigDecimal.class, rs -> rs.getBigDecimal(column)
            ),
            new AbstractMap.SimpleImmutableEntry<>(
                UUID.class, rs -> rs.getObject(column, UUID.class)
            )
        ).collect(
            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
        );
    }
}
