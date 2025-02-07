/*
 * Copyright (c) 2012-2025 Yegor Bugayenko
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.jdbc;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Default mappings for types.
 *
 * @since 0.17.6
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
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
     *
     * @param column Column position.
     */
    DefaultMappings(final int column) {
        this(
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
        );
    }

    /**
     * Ctor.
     *
     * @param mappings Mappings.
     */
    @SafeVarargs
    private DefaultMappings(
        final Map.Entry<Class<?>, Outcome.Mapping<?>>... mappings
    ) {
        this(
            Stream
                .of(mappings)
                .collect(
                    Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                    )
                )
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> Outcome.Mapping<X> forType(final Class<? extends X> tpe) {
        if (!this.map.containsKey(tpe)) {
            throw new IllegalArgumentException(
                String.format("Type %s is not supported", tpe.getName())
            );
        }
        return (Outcome.Mapping<X>) this.map.get(tpe);
    }
}
