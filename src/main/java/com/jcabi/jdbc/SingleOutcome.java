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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.UUID;
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
 * {@link Byte}, {@link Date}, {@link UUID}, and {@link Utc}.
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
@EqualsAndHashCode(of = {"mapping", "silently"})
@RequiredArgsConstructor
public final class SingleOutcome<T> implements Outcome<T> {

    /**
     * The type.
     */
    private final Mapping<? extends T> mapping;

    /**
     * Silently return NULL if no row found.
     */
    private final boolean silently;

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
        this(
            tpe,
            Outcome.DEFAULT_MAPPINGS,
            slnt
        );
    }

    /**
     * Public ctor.
     *
     * @param tpe The type to convert to
     * @param mps The mappings
     * @param slnt Silently return NULL if there is no row
     */
    public SingleOutcome(final Class<T> tpe, final Mappings mps, final boolean slnt) {
        this(mps.forType(tpe), slnt);
    }

    @Override
    public T handle(final ResultSet rset, final Statement stmt)
        throws SQLException {
        T result = null;
        if (rset.next()) {
            result = this.fetch(rset);
        } else if (!this.silently) {
            throw new SQLException("No records found");
        }
        return result;
    }

    /**
     * Fetch the value from result set.
     *
     * @param rset Result set
     * @return The result
     * @throws SQLException If some error inside
     */
    private T fetch(final ResultSet rset) throws SQLException {
        return this.mapping.map(rset);
    }
}
