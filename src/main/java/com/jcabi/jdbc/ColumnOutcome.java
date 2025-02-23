/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Outcome that returns first column.
 *
 * <p>Use it when you need the first column:
 *
 * <pre> Collection&lgt;Long&gt; salaries = new JdbcSession(source)
 *   .sql("SELECT salary FROM user")
 *   .select(new ColumnOutcome&lt;Long&gt;(Long.class));</pre>
 *
 * <p>Supported types are: {@link String}, {@link Long}, {@link Boolean},
 * {@link Byte}, {@link Date}, {@link UUID}, and {@link Utc}.
 *
 * @param <T> Type of items
 * @since 0.13
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class ColumnOutcome<T> implements Outcome<Collection<T>> {

    /**
     * Mapping.
     */
    private final Mapping<T> mapping;

    /**
     * Public ctor.
     *
     * @param tpe The type to convert to
     */
    public ColumnOutcome(final Class<T> tpe) {
        this(tpe, Outcome.DEFAULT_MAPPINGS);
    }

    /**
     * Public ctor.
     *
     * @param tpe The type to convert to
     * @param mps The mappings.
     */
    public ColumnOutcome(final Class<T> tpe, final Mappings mps) {
        this(mps.forType(tpe));
    }

    @Override
    public Collection<T> handle(final ResultSet rset, final Statement stmt)
        throws SQLException {
        final Collection<T> result = new LinkedList<>();
        while (rset.next()) {
            result.add(this.mapping.map(rset));
        }
        return result;
    }

}
