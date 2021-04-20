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
