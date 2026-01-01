/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Outcome that returns a list.
 *
 * <p>Use it when you need a full collection:
 *
 * <pre> Collection&lgt;User&gt; users = new JdbcSession(source)
 *   .sql("SELECT * FROM user")
 *   .select(
 *     new ListOutcome&lt;User&gt;(
 *       new ListOutcome.Mapping&lt;User&gt;() {
 *         &#64;Override
 *         public User map(final ResultSet rset) throws SQLException {
 *           return new User.Simple(rset.getLong(1), rset.getString(2));
 *         }
 *       }
 *     )
 *   );</pre>
 *
 * @param <T> Type of items
 * @since 0.13
 */
@ToString
@EqualsAndHashCode(of = "mapping")
public final class ListOutcome<T> implements Outcome<List<T>> {

    /**
     * Mapping.
     */
    private final transient ListOutcome.Mapping<T> mapping;

    /**
     * Public ctor.
     * @param mpg Mapping
     */
    public ListOutcome(final ListOutcome.Mapping<T> mpg) {
        this.mapping = mpg;
    }

    @Override
    public List<T> handle(final ResultSet rset, final Statement stmt)
        throws SQLException {
        final List<T> result = new LinkedList<>();
        while (rset.next()) {
            result.add(this.mapping.map(rset));
        }
        return result;
    }

}
