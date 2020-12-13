/*
 * Copyright (c) 2012-2018, jcabi.com
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Outcome that returns first column in the first row.
 *
 * <p>Use it when you need the first column in the first row:
 *
 * <pre> Long id = new JdbcSession(source)
 *   .sql("SELECT id FROM user WHERE name = ?")
 *   .set("Jeff Lebowski")
 *   .select(new SingleOutcome&lt;Long&gt;(Long.class));</pre>
 *
 * <p>Supported types are: {@link String}, {@link Long}, {@link Boolean},
 * {@link Byte}, {@link Date}, and {@link Utc}.
 *
 * <p>By default, the outcome throws {@link SQLException} if no records
 * are found in the {@link ResultSet}. You can change this behavior by using
 * a two-arguments constructor ({@code null} will be returned if
 * {@link ResultSet} is empty):
 *
 * <pre> String name = new JdbcSession(source)
 *   .sql("SELECT name FROM user WHERE id = ?")
 *   .set(555)
 *   .select(new SingleOutcome&lt;Long&gt;(Long.class), true);
 * if (name == null) {
 *   // such a record wasn't found in the database
 * }</pre>
 *
 * @param <T> Type of items
 * @since 0.1.8
 */
@ToString
@EqualsAndHashCode(of = {"type", "silently"})
public final class SingleOutcome<T> implements Outcome<T> {

    /**
     * Per-type extraction methods.
     */
    private static final Map<Class<?>, Fetch<?>> FETCH_MAP = new HashMap<>();

    static {
        FETCH_MAP.put(String.class, rs -> rs.getString(1));
        FETCH_MAP.put(Long.class, rs -> rs.getLong(1));
        FETCH_MAP.put(Boolean.class, rs -> rs.getBoolean(1));
        FETCH_MAP.put(Byte.class, rs -> rs.getByte(1));
        FETCH_MAP.put(Date.class, rs -> rs.getDate(1));
        FETCH_MAP.put(Utc.class, rs -> new Utc(Utc.getTimestamp(rs, 1)));
        FETCH_MAP.put(byte[].class, rs -> rs.getBytes(1));
        FETCH_MAP.put(BigDecimal.class, rs -> rs.getBigDecimal(1));
    }

    /**
     * The type name.
     */
    private final transient String type;

    /**
     * Silently return NULL if no row found.
     */
    private final transient boolean silently;

    /**
     * Public ctor.
     *
     * @param tpe The type to convert to
     */
    public SingleOutcome(final Class<T> tpe) {
        this(tpe, false);
    }

    /**
     * Public ctor.
     *
     * @param tpe The type to convert to
     * @param slnt Silently return NULL if there is no row
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    public SingleOutcome(final Class<T> tpe, final boolean slnt) {
        //@checkstyle BooleanExpressionComplexity (3 lines)
        if (FETCH_MAP.containsKey(tpe)) {
            this.type = tpe.getName();
        } else {
            throw new IllegalArgumentException(
                String.format("type %s is not supported", tpe.getName())
            );
        }
        this.silently = slnt;
    }

    @Override
    public T handle(final ResultSet rset, final Statement stmt)
        throws SQLException {
        T result = null;
        if (rset.next()) {
            result = this.fetch(rset);
        } else if (!this.silently) {
            throw new SQLException("no records found");
        }
        return result;
    }

    /**
     * Fetch the value from result set.
     * @param rset Result set
     * @return The result
     * @throws SQLException If some error inside
     */
    @SuppressWarnings({"unchecked",
        "PMD.CyclomaticComplexity"
    })
    private T fetch(final ResultSet rset) throws SQLException {
        final Class<T> tpe;
        try {
            tpe = (Class<T>) Class.forName(this.type);
            return tpe.cast(FETCH_MAP.getOrDefault(tpe, new Unsupported<>(tpe)).fetch(rset));
        } catch (final ClassNotFoundException ex) {
            throw new IllegalArgumentException(
                String.format("Unknown type: %s", this.type), ex
            );
        }
    }

    /**
     * Fetch object from ResultSet.
     * @param <T> Type of result
     * @since 0.17.6
     */
    private interface Fetch<T> {
        /**
         * Fetch object from result set.
         * @param rset ResultSet
         * @return The result
         * @throws SQLException If error occurs
         */
        T fetch(ResultSet rset) throws SQLException;
    }

    /**
     * Unsupported fetch.
     * @param <T> Unsupported type.
     * @since 0.17.6
     */
    @RequiredArgsConstructor
    private static class Unsupported<T> implements Fetch<T> {
        /**
         * Unsupported type.
         */
        private final Class<T> tpe;

        @Override
        public T fetch(final ResultSet rset) throws SQLException {
            throw new IllegalStateException(
                String.format("type %s is not allowed", this.tpe.getName())
            );
        }
    }
}
