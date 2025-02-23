/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Outcome of ResultSet.
 *
 * <p>The following convenience implementations are provided:
 *
 * <ul>
 *  <li>{@link Outcome#NOT_EMPTY} to check that at least one result row is
 *      returned.
 *  <li>{@link Outcome#VOID} for when you wish to disregard the result.
 *  <li>{@link Outcome#UPDATE_COUNT} to check the number of updated rows.
 * </ul>
 *
 * @param <T> Type of expected result
 * @since 0.12
 */
public interface Outcome<T> {

    /**
     * Returns {@code TRUE} if at least one SQL record found in
     * {@link ResultSet}.
     *
     * <p>The outcome returns the value of {@link ResultSet#next()} and throws
     * {@link SQLException} in case of a problem.
     *
     * @since 0.12
     */
    Outcome<Boolean> NOT_EMPTY = (rset, stmt) -> rset.next();

    /**
     * Outcome that does nothing (and always returns {@code null}).
     *
     * <p>Useful when you're not interested in the result:
     *
     * <pre> new JdbcSession(source)
     *   .sql("INSERT INTO foo (name) VALUES (?)")
     *   .set("Jeff Lebowski")
     *   .insert(Outcome.VOID);</pre>
     *
     * @since 0.12
     */
    Outcome<Void> VOID = (rset, stmt) -> Void.TYPE.cast(null);

    /**
     * Outcome that returns the number of updated rows.
     *
     * <p>Use it when you need to determine the number of rows updated:
     *
     * <pre> Integer count = new JdbcSession(source)
     *   .sql("UPDATE employee SET salary = 35000 WHERE department = ?")
     *   .set("Finance")
     *   .update(Outcome.UPDATE_COUNT);</pre>
     *
     * @since 0.12
     */
    Outcome<Integer> UPDATE_COUNT = (rset, stmt) -> stmt.getUpdateCount();

    /**
     * Outcome that returns last insert ID.
     *
     * <p>Use it when you need to get last insert ID from INSERT:
     *
     * <pre> long id = new JdbcSession(source)
     *   .sql("INSERT INTO employee (name) VALUES (?)")
     *   .set("Jeffrey")
     *   .insert(Outcome.LAST_INSERT_ID);</pre>
     *
     * @since 0.13
     */
    Outcome<Long> LAST_INSERT_ID = (rset, stmt) -> {
        if (!rset.next()) {
            throw new SQLException("no last_insert_id() available");
        }
        return rset.getLong(1);
    };

    /**
     * Default mappings.
     *
     * @since 0.17.6
     */
    Mappings DEFAULT_MAPPINGS = new DefaultMappings();

    /**
     * Process the result set and return some value.
     *
     * @param rset The result set to process
     * @param stmt The statement used in the run
     * @return The result
     * @throws SQLException If something goes wrong inside
     */
    T handle(ResultSet rset, Statement stmt) throws SQLException;

    /**
     * Mapping.
     *
     * @param <T> Type of output
     * @since 0.13
     */
    interface Mapping<T> {
        /**
         * Map.
         *
         * @param rset Result set
         * @return Object
         * @throws SQLException If fails
         */
        T map(ResultSet rset) throws SQLException;
    }

    /**
     * Mappings for different types.
     *
     * @since 0.17.6
     */
    interface Mappings {
        /**
         * Mapping for a type.
         *
         * @param tpe Class of result.
         * @param <T> Type of result.
         * @return Mapping.
         */
        <T> Mapping<T> forType(Class<? extends T> tpe);
    }
}
